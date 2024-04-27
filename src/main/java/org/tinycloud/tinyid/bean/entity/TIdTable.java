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
 * @since 2024-04-2024/4/27 20:13
 */
@Getter
@Setter
@ToString
public class TIdTable implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private Long id;

    private String idCode;

    private String idName;

    private Long idValue;

    private Integer idLength;

    private Integer idStep;

    private Integer hasPrefix;

    private String idPrefix;

    private Integer hasSuffix;

    private String idSuffix;

    private String remark;

    private Date createdAt;

    private Date updatedAt;

    private String createdBy;

    private String updatedBy;
}
