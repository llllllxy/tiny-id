package org.tinycloud.tinyid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tinycloud.tinyid.bean.vo.IdTableVo;
import org.tinycloud.tinyid.model.ApiResult;
import org.tinycloud.tinyid.service.DashboardService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-06-12 14:49
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/quantity")
    public ApiResult<Map<String, Object>> quantity() {
        return ApiResult.success(dashboardService.quantity(), "获取成功!");
    }

    @GetMapping(value = "/topList")
    public ApiResult<List<IdTableVo>> topList() {
        return ApiResult.success(dashboardService.topList(), "查询成功");
    }
}
