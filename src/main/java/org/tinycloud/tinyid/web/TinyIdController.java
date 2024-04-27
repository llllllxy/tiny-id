package org.tinycloud.tinyid.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tinycloud.tinyid.model.ApiResult;
import org.tinycloud.tinyid.utils.snowflake.SnowflakeSingleton;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 14:11
 */
@RestController
public class TinyIdController {

    @RequestMapping(value = "/api/snowflake/get", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<Long> getSnowflakeId() {
        return ApiResult.success(SnowflakeSingleton.nextLongId(), "获取成功");
    }
}
