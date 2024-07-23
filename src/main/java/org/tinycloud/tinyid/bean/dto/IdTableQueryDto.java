package org.tinycloud.tinyid.bean.dto;

import lombok.Getter;
import lombok.Setter;
import org.tinycloud.tinyid.model.BasePageDto;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-07-16 15:45
 */
@Setter
@Getter
public class IdTableQueryDto extends BasePageDto {

    private String idCode;

    private String idName;
}
