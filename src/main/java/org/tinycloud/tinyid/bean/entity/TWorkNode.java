package org.tinycloud.tinyid.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-2024/4/27 11:32
 */
@Getter
@Setter
public class TWorkNode implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private Long id;

    private String serverIp;

    private Integer serverPort;

    private Long datacenterId;

    private Long workerId;

    private Date createdAt;

    private Date updatedAt;

    private Date signedAt;
}
