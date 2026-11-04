package org.tinycloud.tinyid.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tinycloud.tinyid.bean.dto.MailConfigEditDto;
import org.tinycloud.tinyid.bean.vo.MailConfigVo;
import org.tinycloud.tinyid.model.ApiResult;
import org.tinycloud.tinyid.service.MailConfigService;

/**
 * <p>
 * 邮件配置控制器：维护雪花节点告警所需的 SMTP 配置
 * </p>
 *
 * @author liuxingyu01
 * @since 2026-11-04
 */
@RestController
@RequestMapping("/mailconfig")
public class MailConfigController {

    @Autowired
    private MailConfigService mailConfigService;

    /**
     * 获取当前邮件配置
     */
    @GetMapping("/detail")
    public ApiResult<MailConfigVo> detail() {
        return ApiResult.success(this.mailConfigService.detail(), "获取成功");
    }

    /**
     * 保存邮件配置
     */
    @PostMapping("/edit")
    public ApiResult<Boolean> edit(@Validated @RequestBody MailConfigEditDto dto) {
        return ApiResult.success(this.mailConfigService.edit(dto), "修改成功");
    }
}
