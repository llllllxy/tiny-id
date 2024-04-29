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

    @Transactional
    public TIdTable refreshByIdCode(String idCode) {
        // 这条语句自带行锁，相当于 select for update了
        String sql2 = "UPDATE t_idtable SET id_value = id_value + id_step WHERE id_code = ?";
        int num2 = this.jdbcTemplate.update(sql2, idCode);

        String sql3 = "SELECT * FROM t_idtable WHERE id_code = ?";
        TIdTable idTable = this.jdbcTemplate.queryForObject(sql3, BeanPropertyRowMapper.newInstance(TIdTable.class), idCode);
        return idTable;
    }

    public List<TIdTable> listAll() {
        String sql = "SELECT * FROM t_idtable";
        List<TIdTable> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TIdTable.class));
        return list;
    }

    public TIdTable get(String idCode) {
        String sql = "SELECT * FROM t_idtable WHERE id_code = ?";
        List<TIdTable> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TIdTable.class), idCode);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }
}