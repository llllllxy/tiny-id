package org.tinycloud.tinyid.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tinycloud.tinyid.model.ApiResult;
import org.tinycloud.tinyid.utils.IdTableUtils;
import org.tinycloud.tinyid.utils.snowflake.SnowflakeSingleton;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 14:11
 */
@RestController
@RequestMapping("/api")
public class TinyIdController {

    @RequestMapping(value = "/snowflake/get", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<Long> getSnowflakeId() {
        return ApiResult.success(SnowflakeSingleton.nextLongId(), "获取成功");
    }

    @RequestMapping(value = "/snowflake/batch/{batchSize}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<List<Long>> getSnowflakeId(@PathVariable("batchSize") Integer batchSize) {
        return ApiResult.success(SnowflakeSingleton.nextBatchLongId(batchSize), "批量获取成功");
    }

    @RequestMapping(value = "/segment/get/{idCode}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<String> getSegment(@PathVariable("idCode") String idCode) {
        return ApiResult.success(IdTableUtils.nextId(idCode), "获取成功");
    }

    @RequestMapping(value = "/segment/batch/{idCode}/{batchSize}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<List<String>> getSegment(@PathVariable("idCode") String idCode,
                                              @PathVariable("batchSize") Integer batchSize) {
        return ApiResult.success(IdTableUtils.nextBatchId(idCode, batchSize), "批量获取成功");
    }

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<String> hhh() {
        return ApiResult.success(null, "测试成功");
    }
}
