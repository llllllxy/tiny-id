package org.tinycloud.tinyid.bean.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 11:32
 */
@Getter
@Setter
@ToString
public class TWorkNode implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private Long id;

    private String serverIp;

    private Integer serverPort;

    private Long datacenterId;

    private Long workerId;

    private Long totalId;

    private Date createdAt;

    private Date updatedAt;

    private Date signedAt;
}
