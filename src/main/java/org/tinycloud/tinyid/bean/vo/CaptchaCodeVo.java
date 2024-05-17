package org.tinycloud.tinyid.bean.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-05-15 18:53
 */
@Data
public class CaptchaCodeVo implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private String uuid;

    private String img;
}
