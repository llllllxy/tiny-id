package org.tinycloud.tinyid.bean.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 删除流水号配置-DTO
 * </p>
 *
 * @author liuxingyu01
 * @since 2026-11-04
 */
@Setter
@Getter
public class IdTableDelDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 流水号配置主键
     */
    @NotNull(message = "主键id不能为空")
    private Long id;
}
