package org.tinycloud.tinyid.bean.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-07-24 14:52
 */
@Setter
@Getter
public class IdTableEditDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    @NotNull(message = "流水号主键不能为空")
    private Long id;

    @NotEmpty(message = "流水号名称不能为空")
    private String idName;

    @NotNull(message = "流水号长度不能为空")
    private Integer idLength;

    @NotNull(message = "流水号步长不能为空")
    private Integer idStep;

    @NotNull(message = "是否包含前缀不能为空")
    private Integer hasPrefix;

    @Length(max = 32, min = 0, message = "前缀不能超过32个字符")
    private String idPrefix;

    @NotNull(message = "是否包含后缀不能为空")
    private Integer hasSuffix;

    @Length(max = 32, min = 0, message = "后缀不能超过32个字符")
    private String idSuffix;

    @Length(max = 255, min = 0, message = "后缀不能超过255个字符")
    private String remark;
}
