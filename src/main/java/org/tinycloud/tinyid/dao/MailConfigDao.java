package org.tinycloud.tinyid.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.tinycloud.tinyid.bean.entity.TMailConfig;

import java.util.List;

/**
 * <p>
 * 邮件配置DAO：维护 t_mail_config 表（系统中仅保存一条未删除的配置记录）
 * </p>
 *
 * @author liuxingyu01
 * @since 2026-11-04
 */
@Repository
public class MailConfigDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询当前生效的邮件配置（未删除的那一条）
     *
     * @return TMailConfig，未配置时返回null
     */
    public TMailConfig get() {
        String sql = "SELECT * FROM t_mail_config WHERE del_flag = 0";
        List<TMailConfig> list = this.jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(TMailConfig.class));
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 新增邮件配置
     *
     * @param mailConfig 配置内容
     * @param createdBy  创建人（当前登录人）
     * @return 是否成功
     */
    public boolean insert(TMailConfig mailConfig, String createdBy) {
        String sql = "INSERT INTO t_mail_config (smtp_address, smtp_port, email_account, email_password, receive_email, ssl_flag, del_flag, created_by) VALUES (?,?,?,?,?,?,0,?)";
        int num = this.jdbcTemplate.update(sql, mailConfig.getSmtpAddress(), mailConfig.getSmtpPort(),
                mailConfig.getEmailAccount(), mailConfig.getEmailPassword(), mailConfig.getReceiveEmail(),
                mailConfig.getSslFlag(), createdBy);
        return num > 0;
    }

    /**
     * 更新邮件配置
     *
     * @param mailConfig 配置内容（需含id）
     * @param updatedBy  更新人（当前登录人）
     * @return 是否成功
     */
    public boolean update(TMailConfig mailConfig, String updatedBy) {
        String sql = "UPDATE t_mail_config SET smtp_address = ?, smtp_port = ?, email_account = ?, email_password = ?, receive_email = ?, ssl_flag = ?, updated_by = ? WHERE id = ?";
        int num = this.jdbcTemplate.update(sql, mailConfig.getSmtpAddress(), mailConfig.getSmtpPort(),
                mailConfig.getEmailAccount(), mailConfig.getEmailPassword(), mailConfig.getReceiveEmail(),
                mailConfig.getSslFlag(), updatedBy, mailConfig.getId());
        return num > 0;
    }
}
