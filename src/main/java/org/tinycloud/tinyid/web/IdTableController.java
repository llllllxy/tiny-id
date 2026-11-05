package org.tinycloud.tinyid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tinycloud.tinyid.bean.dto.IdTableAddDto;
import org.tinycloud.tinyid.bean.dto.IdTableDelDto;
import org.tinycloud.tinyid.bean.dto.IdTableEditDto;
import org.tinycloud.tinyid.bean.dto.IdTableQueryDto;
import org.tinycloud.tinyid.bean.vo.IdTableVo;
import org.tinycloud.tinyid.model.ApiResult;
import org.tinycloud.tinyid.model.PageModel;
import org.tinycloud.tinyid.service.IdTableService;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-07-23 14:47
 */
@RestController
@RequestMapping("/idtable")
public class IdTableController {

    @Autowired
    private IdTableService idTableService;

    @PostMapping("/query")
    public ApiResult<PageModel<IdTableVo>> query(@RequestBody IdTableQueryDto dto) {
        return ApiResult.success(idTableService.query(dto), "查询成功!");
    }

    /**
     * 删除流水号配置（状态变更操作使用POST+JSON body，与其他写接口风格一致，避免GET形式被预取/误触发）
     */
    @PostMapping("/del")
    public ApiResult<Boolean> delete(@Validated @RequestBody IdTableDelDto dto) {
        return ApiResult.success(idTableService.delete(dto.getId()), "删除成功!");
    }

    @PostMapping("/add")
    public ApiResult<Boolean> add(@Validated @RequestBody IdTableAddDto dto) {
        return ApiResult.success(idTableService.add(dto), "新增成功!");
    }

    @PostMapping("/edit")
    public ApiResult<Boolean> edit(@Validated @RequestBody IdTableEditDto dto) {
        return ApiResult.success(idTableService.edit(dto), "修改成功!");
    }
}
