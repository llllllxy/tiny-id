package org.tinycloud.tinyid.web;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tinycloud.tinyid.bean.dto.AuthEditInfoDto;
import org.tinycloud.tinyid.bean.dto.AuthEditPasswordDto;
import org.tinycloud.tinyid.bean.dto.AuthLoginDto;
import org.tinycloud.tinyid.bean.vo.CaptchaCodeVo;
import org.tinycloud.tinyid.bean.vo.UserInfoVo;
import org.tinycloud.tinyid.constant.GlobalConstant;
import org.tinycloud.tinyid.model.ApiResult;
import org.tinycloud.tinyid.service.AuthService;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统会话控制器
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-06-01 15:00
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    final static Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @GetMapping("/getCode")
    @ResponseBody
    public ApiResult<CaptchaCodeVo> getCode(HttpSession session) {
        return ApiResult.success(authService.getCode(session), "获取验证码成功");
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public ApiResult<?> login(@Validated @RequestBody AuthLoginDto authLoginDto, HttpSession session) {
        // 进行用户名和密码校验
        authService.authentication(authLoginDto);
        // 把username作为loginId，执行会话创建操作
        session.setAttribute(GlobalConstant.SESSION_KEY, authLoginDto.getUsername());
        String token = session.getId();
        logger.info("token = " + token);
        return ApiResult.success(token, "登录成功，欢迎回来！");
    }


    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public ApiResult<?> logout(HttpSession session) {
        session.invalidate();
        return ApiResult.success("退出登录成功！");
    }


    /**
     * 会话校验
     */
    @GetMapping("/getUsername")
    public ApiResult<?> getUsername(HttpSession session) {
        return ApiResult.success(session.getAttribute(GlobalConstant.SESSION_KEY), "获取成功");
    }

    /**
     * 获取当前会话信息
     */
    @GetMapping("/getUserInfo")
    public ApiResult<UserInfoVo> getUserInfo() {
        return ApiResult.success(authService.getUserInfo(), "获取成功");
    }

    /**
     * 菜单初始化
     */
    @GetMapping("/init")
    public ApiResult<?> init() {
        Map<String, Object> initInfo = new HashMap<>();

        Map<String, String> menuItem0 = new HashMap<>();
        menuItem0.put("title", "仪表盘");
        menuItem0.put("href", "page/dashboard.html");
        menuItem0.put("icon", "fa fa-navicon");
        menuItem0.put("target", "_self");

        Map<String, String> menuItem1 = new HashMap<>();
        menuItem1.put("title", "雪花ID节点");
        menuItem1.put("href", "page/worknode.html");
        menuItem1.put("icon", "fa fa-navicon");
        menuItem1.put("target", "_self");

        Map<String, String> menuItem2 = new HashMap<>();
        menuItem2.put("title", "流水号管理");
        menuItem2.put("href", "page/idtable.html");
        menuItem2.put("icon", "fa fa-navicon");
        menuItem2.put("target", "_self");

        Map<String, String> menuItem3 = new HashMap<>();
        menuItem3.put("title", "使用文档");
        menuItem3.put("href", "page/doc.html");
        menuItem3.put("icon", "fa fa-navicon");
        menuItem3.put("target", "_self");

        List<Map<String, String>> menuList = new ArrayList<>();
        menuList.add(menuItem0);
        menuList.add(menuItem1);
        menuList.add(menuItem2);
        menuList.add(menuItem3);

        Map<String, String> homeInfo = new HashMap<>();
        homeInfo.put("title", "首页");
        homeInfo.put("href", "page/welcome.html");

        Map<String, String> logoInfo = new HashMap<>();
        logoInfo.put("title", "TINY-ID控制台");
        logoInfo.put("image", "/images/logo.png");
        logoInfo.put("href", "");

        initInfo.put("homeInfo", homeInfo);
        initInfo.put("logoInfo", logoInfo);
        initInfo.put("menuInfo", menuList);

        return ApiResult.success(initInfo, "获取成功");
    }


    /**
     * 修改密码
     */
    @PostMapping("/editPassword")
    public ApiResult<?> editPassword(@Validated @RequestBody AuthEditPasswordDto dto, HttpSession session) {
        boolean result = authService.editPassword(dto);
        if (result) {
            session.invalidate();
        }
        return ApiResult.success(result, "修改密码成功！");
    }

    /**
     * 修改账户信息
     */
    @PostMapping("/setting")
    public ApiResult<?> setting(@Validated @RequestBody AuthEditInfoDto dto) {
        boolean result = authService.setting(dto);
        return ApiResult.success(result, "修改密码成功！");
    }
}
