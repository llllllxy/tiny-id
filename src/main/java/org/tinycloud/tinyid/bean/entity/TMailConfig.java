package org.tinycloud.tinyid.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 邮件配置实体：对应 t_mail_config 表，存储 SMTP 告警邮件的发件与收件配置
 * </p>
 *
 * @author liuxingyu01
 * @since 2026-11-04
 */
@Getter
@Setter
public class TMailConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键，内码
     */
    private Long id;

    /**
     * smtp服务器地址
     */
    private String smtpAddress;

    /**
     * smtp端口
     */
    private String smtpPort;

    /**
     * 邮箱账号
     */
    private String emailAccount;

    /**
     * 邮箱密码
     */
    private String emailPassword;

    /**
     * 收件邮箱地址(多个用逗号隔开)
     */
    private String receiveEmail;

    /**
     * 是否开启 ssl（0-- 开启 --1 关闭）
     */
    private String sslFlag;

    /**
     * 删除标志（0--未删除1--已删除）
     */
    private Integer delFlag;

    /**
     * 备注描述信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 创建人-对应t_user.username
     */
    private String createdBy;

    /**
     * 更新人-对应t_user.username
     */
    private String updatedBy;
}
