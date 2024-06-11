package org.tinycloud.tinyid.bean.dto;

import lombok.Getter;
import lombok.Setter;
import org.tinycloud.tinyid.model.BasePageDto;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-05-29 14:57
 */
@Setter
@Getter
public class WorkNodeQueryDto extends BasePageDto {
    private String serverIp;
}
