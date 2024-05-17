package org.tinycloud.tinyid.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 系统用户实体类
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-06-01 14:54
 */
@Getter
@Setter
public class TUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态（0--正常 1--冻结）
     */
    private Integer status;

    /**
     * 邮箱
     */
    private String avatar;

    /**
     * 删除标志（0--未删除1--已删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
