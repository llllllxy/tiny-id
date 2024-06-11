package org.tinycloud.tinyid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tinycloud.tinyid.bean.dto.WorkNodeQueryDto;
import org.tinycloud.tinyid.bean.vo.WorkNodeVo;
import org.tinycloud.tinyid.model.ApiResult;
import org.tinycloud.tinyid.model.PageModel;
import org.tinycloud.tinyid.service.WorkNodeService;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-05-29 14:52
 */
@RestController
@RequestMapping("/worknode")
public class WorkNodeController {

    @Autowired
    private WorkNodeService workNodeService;

    @PostMapping("/query")
    public ApiResult<PageModel<WorkNodeVo>> query(@RequestBody WorkNodeQueryDto dto) {
        return ApiResult.success(workNodeService.query(dto), "查询成功!");
    }
}
