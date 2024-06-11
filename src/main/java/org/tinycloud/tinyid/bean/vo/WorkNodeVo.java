package org.tinycloud.tinyid.bean.vo;

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
public class WorkNodeVo {

    private Long id;

    private String serverIp;

    private Integer serverPort;

    private Long datacenterId;

    private Long workerId;

    private Date createdAt;

    private Date updatedAt;

    private Date signedAt;
}
