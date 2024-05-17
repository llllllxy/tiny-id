package org.tinycloud.tinyid.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.tinycloud.tinyid.bean.dto.AuthEditInfoDto;
import org.tinycloud.tinyid.bean.entity.TUser;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-05-15 17:51
 */
@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public TUser getUserByUsername(String username) {
        String sql = "SELECT * FROM t_user WHERE username = ? and del_flag = 0";
        List<TUser> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TUser.class), username);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public int updatePassword(String username, String password) {
        String sql2 = "UPDATE t_user SET password = ? WHERE username = ?";
        int num2 = this.jdbcTemplate.update(sql2, password, username);
        return num2;
    }

    public int settingInfo(AuthEditInfoDto dto) {
        String sql2 = "UPDATE t_user SET nickname = ?, phone = ?, email = ? WHERE username = ?";
        int num2 = this.jdbcTemplate.update(sql2, dto.getNickname(), dto.getPhone(), dto.getEmail(), dto.getUsername());
        return num2;
    }
}
