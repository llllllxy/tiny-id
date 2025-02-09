package org.tinycloud.tinyid.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.tinycloud.tinyid.bean.dto.WorkNodeQueryDto;
import org.tinycloud.tinyid.bean.entity.TWorkNode;
import org.tinycloud.tinyid.utils.IdExtractor;
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
@Repository
public class WorkNodeDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 获取当前数据中心数量
     *
     * @return 数量
     */
    public Long getDistinctDatacenterIds() {
        String sql = "SELECT COUNT(*) FROM t_worker_node";
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
        String sql = "UPDATE t_worker_node SET signed_at = ? WHERE datacenter_id = ? AND worker_id = ?";
        int num = this.jdbcTemplate.update(sql, new Date(), SnowflakeSingleton.datacenterId, SnowflakeSingleton.workerId);
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
