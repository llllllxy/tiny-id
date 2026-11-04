package org.tinycloud.tinyid.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.tinycloud.tinyid.bean.dto.WorkNodeQueryDto;
import org.tinycloud.tinyid.bean.entity.TWorkNode;
import org.tinycloud.tinyid.utils.IdExtractor;
import org.tinycloud.tinyid.utils.LocalHostUtils;
import org.tinycloud.tinyid.utils.snowflake.SnowflakeSingleton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 10:58
 */
@Slf4j
@Repository
public class WorkNodeDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 本机注册用的服务端口，与启动时注册保持一致
     */
    @Value("${server.port}")
    private Integer serverPort;


    /**
     * 获取当前已占用的节点组合数量（去重后的 total_id 数量）
     *
     * @return 数量
     */
    public Long getUsedTotalIdCount() {
        String sql = "SELECT COUNT(DISTINCT total_id) FROM t_worker_node";
        Long count = this.jdbcTemplate.queryForObject(sql, Long.class);
        return count;
    }

    /**
     * 根据数据中心id，来获取机器id列表
     *
     * @param datacenterId 数据中心IP
     * @return 已有的Node列表
     */
    public List<TWorkNode> getWorkNodeListByDatacenterId(long datacenterId) {
        String sql = "SELECT * FROM t_worker_node WHERE datacenter_id = ?";
        List<TWorkNode> nodeList = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TWorkNode.class), datacenterId);
        return nodeList;
    }

    /**
     * 查询所有 id 并返回 List<Long>
     */
    public List<Long> getTotalIds() {
        String sql = "SELECT total_id FROM t_worker_node";
        return jdbcTemplate.queryForList(sql, Long.class);
    }

    /**
     * 保存WORKER_NODE
     *
     * @param serverIp     服务IP
     * @param severPort    服务端口
     * @param workerId     机器ID
     * @param datacenterId 数据中心IP
     */
    public void addWorkNode(String serverIp, Integer severPort, long workerId, long datacenterId) {
        // 重新计算回totalId
        long totalId = (int) IdExtractor.generateTotalId((int) datacenterId, (int) workerId);
        String sql = "INSERT INTO t_worker_node (server_ip, server_port, worker_id, datacenter_id, total_id, signed_at) VALUES (?,?,?,?,?,?)";
        this.jdbcTemplate.update(sql, serverIp, severPort, workerId, datacenterId, totalId, new Date());
    }

    /**
     * 属性记录时间
     *
     * @return 成功true, 失败false
     */
    public boolean sign() {
        if (!SnowflakeSingleton.isInitialized()) {
            return false;
        }
        String sql = "UPDATE t_worker_node SET signed_at = ? WHERE datacenter_id = ? AND worker_id = ?";
        int num = this.jdbcTemplate.update(sql, new Date(), SnowflakeSingleton.getDatacenterId(), SnowflakeSingleton.getWorkerId());
        if (num == 0) {
            // 心跳行不存在：典型场景是 DB 长时间断连（雪花发号不依赖DB，服务仍在运行），
            // 恢复后心跳行被 clean 任务清理。必须立即重新注册自己，
            // 否则本节点的 (datacenterId, workerId) 会被新实例注册复用，造成雪花ID重复
            log.warn("节点心跳行不存在，尝试重新注册, datacenterId={}, workerId={}",
                    SnowflakeSingleton.getDatacenterId(), SnowflakeSingleton.getWorkerId());
            try {
                this.addWorkNode(LocalHostUtils.getInetAddress().getHostAddress(), serverPort,
                        SnowflakeSingleton.getWorkerId(), SnowflakeSingleton.getDatacenterId());
                log.warn("节点重新注册成功, datacenterId={}, workerId={}",
                        SnowflakeSingleton.getDatacenterId(), SnowflakeSingleton.getWorkerId());
                return true;
            } catch (DuplicateKeyException e) {
                // 身份组合已被其它实例抢注：本节点曾失联超期且ID已被占用，继续发号有重复风险，需人工介入
                log.error("节点重新注册失败，(datacenterId, workerId) 已被其它实例占用，存在雪花ID重复风险，" +
                        "请立即下线本节点并重新分配, datacenterId={}, workerId={}",
                        SnowflakeSingleton.getDatacenterId(), SnowflakeSingleton.getWorkerId());
                return false;
            } catch (Exception e) {
                log.error("节点重新注册异常, datacenterId={}, workerId={}",
                        SnowflakeSingleton.getDatacenterId(), SnowflakeSingleton.getWorkerId(), e);
                return false;
            }
        }
        return num > 0;
    }

    /**
     * 清楚过期的雪花节点信息（signed_at时间在一天前的记录）
     *
     * @return 成功true, 失败false
     */
    public boolean clean() {
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 获取一天以前的时间
        LocalDateTime oneDayAgoTime = currentTime.minusDays(1);
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String oneDayAgoTimeStr = oneDayAgoTime.format(formatter);

        String sql = "DELETE FROM t_worker_node WHERE signed_at < ?";
        int num = this.jdbcTemplate.update(sql, oneDayAgoTimeStr);
        return num > 0;
    }

    /**
     * 分页查询list
     *
     * @param dto WorkNodeQueryDto
     * @return 节点列表
     */
    public List<TWorkNode> pageList(WorkNodeQueryDto dto) {
        List<Object> perimeters = new ArrayList<>();
        String sql = "SELECT * FROM t_worker_node ";
        if (StringUtils.hasText(dto.getServerIp())) {
            sql = sql + "WHERE server_ip LIKE CONCAT('%', ? ,'%')";
            perimeters.add(dto.getServerIp());
        }
        int offset = (dto.getPageNo() - 1) * dto.getPageSize();
        int limit = dto.getPageSize();
        sql = sql + "LIMIT ?,?";
        perimeters.add(offset);
        perimeters.add(limit);

        List<TWorkNode> nodeList = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TWorkNode.class), perimeters.toArray());
        return nodeList;
    }

    /**
     * 分页查询count
     *
     * @param dto WorkNodeQueryDto
     * @return 总条数
     */
    public Long pageCount(WorkNodeQueryDto dto) {
        List<Object> perimeters = new ArrayList<>();
        String sql = "SELECT count(*) FROM t_worker_node ";
        if (StringUtils.hasText(dto.getServerIp())) {
            sql = sql + "WHERE server_ip LIKE CONCAT('%', ? ,'%')";
            perimeters.add(dto.getServerIp());
        }
        Long totalSize = jdbcTemplate.queryForObject(sql, Long.class, perimeters.toArray());
        return totalSize;
    }

    /**
     * 获取总数
     *
     * @return 节点总数
     */
    public Long totalCount() {
        String sql = "SELECT count(*) FROM t_worker_node";
        Long totalSize = jdbcTemplate.queryForObject(sql, Long.class);
        return totalSize;
    }
}
