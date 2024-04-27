package org.tinycloud.tinyid.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tinycloud.tinyid.model.ApiResult;
import org.tinycloud.tinyid.utils.IdTableUtils;
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

    @RequestMapping(value = "/api/segment/get/{idCode}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<String> getSegment(@PathVariable("idCode") String idCode) {
        return ApiResult.success(IdTableUtils.nextId(idCode), "获取成功");
    }
}
