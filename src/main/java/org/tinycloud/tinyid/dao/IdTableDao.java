package org.tinycloud.tinyid.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.tinycloud.tinyid.bean.entity.TIdTable;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-2024/4/27 20:20
 */
@Slf4j
@Repository
public class IdTableDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 刷新流水号步值
     *
     * @param idCode 编码
     * @return TIdTable
     */
    @Transactional
    public TIdTable refreshByIdCode(String idCode) {
        // 这条语句自带行锁，相当于 select for update了
        String sql2 = "UPDATE t_idtable SET id_value = id_value + id_step WHERE id_code = ?";
        int num2 = this.jdbcTemplate.update(sql2, idCode);

        String sql3 = "SELECT * FROM t_idtable WHERE id_code = ?";
        TIdTable idTable = this.jdbcTemplate.queryForObject(sql3, BeanPropertyRowMapper.newInstance(TIdTable.class), idCode);
        return idTable;
    }

    /**
     * 获取所有流水号信息
     *
     * @return TIdTable列表
     */
    public List<TIdTable> listAll() {
        String sql = "SELECT * FROM t_idtable";
        List<TIdTable> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TIdTable.class));
        return list;
    }

    /**
     * 获取单条流水号信息
     *
     * @param idCode 编码
     * @return TIdTable
     */
    public TIdTable get(String idCode) {
        String sql = "SELECT * FROM t_idtable WHERE id_code = ?";
        List<TIdTable> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TIdTable.class), idCode);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 获取总数
     *
     * @return 流水号总数
     */
    public Long totalCount() {
        String sql = "SELECT count(*) FROM t_idtable";
        Long totalSize = jdbcTemplate.queryForObject(sql, Long.class);
        return totalSize;
    }

    /**
     * 获取id_value最大的前流水号信息
     *
     * @return TIdTable列表
     */
    public List<TIdTable> listTop25() {
        String sql = "SELECT * FROM t_idtable ORDER BY id_value DESC LIMIT 25";
        List<TIdTable> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TIdTable.class));
        return list;
    }
}
