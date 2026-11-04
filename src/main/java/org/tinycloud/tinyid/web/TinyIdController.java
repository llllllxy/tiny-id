package org.tinycloud.tinyid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tinycloud.tinyid.constant.GlobalConstant;
import org.tinycloud.tinyid.enums.CoreErrorCode;
import org.tinycloud.tinyid.exception.CoreException;
import org.tinycloud.tinyid.model.ApiResult;
import org.tinycloud.tinyid.service.SegmentIdService;
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

    @Autowired
    private SegmentIdService segmentIdService;

    @RequestMapping(value = "/snowflake/get", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<Long> getSnowflakeId() {
        return ApiResult.success(SnowflakeSingleton.nextLongId(), "获取成功");
    }

    @RequestMapping(value = "/snowflake/batch/{batchSize}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<List<Long>> getSnowflakeId(@PathVariable("batchSize") Integer batchSize) {
        // 校验批量大小，防止超大批量请求长时间占用请求线程
        if (batchSize == null || batchSize < 1 || batchSize > GlobalConstant.MAX_BATCH_SIZE) {
            throw new CoreException(CoreErrorCode.THE_BATCH_SIZE_IS_INVALID);
        }
        return ApiResult.success(SnowflakeSingleton.nextBatchLongId(batchSize), "批量获取成功");
    }

    @RequestMapping(value = "/segment/get/{idCode}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<String> getSegment(@PathVariable("idCode") String idCode) {
        return ApiResult.success(segmentIdService.nextId(idCode), "获取成功");
    }

    @RequestMapping(value = "/segment/batch/{idCode}/{batchSize}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<List<String>> getSegment(@PathVariable("idCode") String idCode,
                                              @PathVariable("batchSize") Integer batchSize) {
        return ApiResult.success(segmentIdService.nextBatchId(idCode, batchSize), "批量获取成功");
    }

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResult<String> hhh() {
        return ApiResult.success(null, "测试成功");
    }
}
