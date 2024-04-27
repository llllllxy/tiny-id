package org.tinycloud.tinyid.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.tinycloud.tinyid.bean.entity.TWorkNode;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-2024/4/27 10:58
 */
@Repository
public class WorkNodeDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 获取当前数据中心数量
     *
     * @return
     */
    public Long getDistinctDatacenterIds() {
        String sql = "SELECT COUNT(*) FROM t_worker_node";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count;
    }

    /**
     * 根据数据中心id，来获取机器id列表
     *
     * @param datacenterId
     * @return
     */
    public List<TWorkNode> getWorkNodeListByDatacenterId(long datacenterId) {
        String sql = "SELECT * FROM t_worker_node WHERE datacenter_id = ?";
        List<TWorkNode> nodeList = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TWorkNode.class), datacenterId);
        return nodeList;
    }

    public int addWorkNode(String serverIp, Integer severPort, long workerId, long datacenterId) {
        String sql = "INSERT INTO t_worker_node (server_ip, server_port, worker_id, datacenter_id, signed_at) VALUES (?,?,?,?,?)";
        return jdbcTemplate.update(sql, serverIp, severPort, workerId, datacenterId, new Date());
    }
}
