package org.tinycloud.tinyid.service;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.tinycloud.tinyid.bean.dto.AuthEditInfoDto;
import org.tinycloud.tinyid.bean.dto.AuthLoginDto;
import org.tinycloud.tinyid.bean.entity.TUser;
import org.tinycloud.tinyid.constant.GlobalConstant;
import org.tinycloud.tinyid.dao.UserDao;
import org.tinycloud.tinyid.enums.CoreErrorCode;
import org.tinycloud.tinyid.exception.CoreException;
import org.tinycloud.tinyid.utils.secure.BCrypt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthService 登录认证单元测试（纯 Mockito，不依赖数据库）
 *
 * <p>重点覆盖登录验证码校验：正确/错误/过期三种输入路径，以及验证码的一次性消费语义。</p>
 */
class AuthServiceTest {

    private static final String CAPTCHA_UUID = "key1";
    private static final String CAPTCHA_SESSION_KEY = GlobalConstant.CAPTCHA_CODE_SESSION_KEY + CAPTCHA_UUID;

    private AuthService authService;

    private UserDao userDao;

    private HttpSession session;

    @BeforeEach
    void setUp() {
        userDao = Mockito.mock(UserDao.class);
        session = Mockito.mock(HttpSession.class);
        authService = new AuthService();
        ReflectionTestUtils.setField(authService, "userDao", userDao);
    }

    private AuthLoginDto buildLoginDto(String captchaCode) {
        AuthLoginDto dto = new AuthLoginDto();
        dto.setUsername("admin");
        dto.setPassword("pwd");
        dto.setCaptchaKey(CAPTCHA_UUID);
        dto.setCaptchaCode(captchaCode);
        return dto;
    }

    /**
     * 构造一个启用状态的用户，密码为明文 pwd 加盐后的 BCrypt 哈希
     */
    private TUser buildEnabledUser() {
        TUser user = new TUser();
        user.setUsername("admin");
        user.setPassword(BCrypt.hashpw("pwd" + "323@#@$1234da323@#@$1234da", BCrypt.gensalt()));
        user.setStatus(GlobalConstant.ENABLED);
        return user;
    }

    /**
     * 场景：验证码正确。应通过验证码校验并继续走用户名密码校验，最终认证通过。
     */
    @Test
    void correctCaptchaShouldPassAuthentication() {
        when(session.getAttribute(CAPTCHA_SESSION_KEY)).thenReturn("Ab12");
        when(userDao.getUserByUsername("admin")).thenReturn(buildEnabledUser());

        // 忽略大小写比对：Ab12 与 aB12 应视为同一个验证码
        assertDoesNotThrow(() -> authService.authentication(buildLoginDto("aB12"), session));
        verify(session).removeAttribute(CAPTCHA_SESSION_KEY);
    }

    /**
     * 场景：验证码错误。应直接抛验证码错误异常，不再进行用户名密码校验（不打库）。
     */
    @Test
    void wrongCaptchaShouldFailFast() {
        when(session.getAttribute(CAPTCHA_SESSION_KEY)).thenReturn("Ab12");

        CoreException exception = assertThrows(CoreException.class,
                () -> authService.authentication(buildLoginDto("zz99"), session));
        assertEquals(CoreErrorCode.THE_CAPTCHA_CODE_IS_INVALID.getCode(), exception.getCode());
        verify(userDao, Mockito.never()).getUserByUsername("admin");
    }

    /**
     * 场景：验证码已过期（session 中不存在对应 key）。应抛验证码错误异常。
     */
    @Test
    void expiredCaptchaShouldFail() {
        when(session.getAttribute(CAPTCHA_SESSION_KEY)).thenReturn(null);

        CoreException exception = assertThrows(CoreException.class,
                () -> authService.authentication(buildLoginDto("Ab12"), session));
        assertEquals(CoreErrorCode.THE_CAPTCHA_CODE_IS_INVALID.getCode(), exception.getCode());
    }

    /**
     * 场景：验证码一次性消费。同一验证码第一次认证通过后立即失效，
     * 第二次即使答案完全正确也必须被拒绝（防重放）。
     */
    @Test
    void captchaShouldBeConsumedAfterFirstUse() {
        when(session.getAttribute(CAPTCHA_SESSION_KEY)).thenReturn("Ab12");
        when(userDao.getUserByUsername("admin")).thenReturn(buildEnabledUser());

        assertDoesNotThrow(() -> authService.authentication(buildLoginDto("Ab12"), session));
        // 模拟服务端已消费：第二次取不到验证码
        when(session.getAttribute(CAPTCHA_SESSION_KEY)).thenReturn(null);
        assertThrows(CoreException.class, () -> authService.authentication(buildLoginDto("Ab12"), session));
    }

    /**
     * 场景：修改资料防水平越权。客户端伪造 username 指向他人（如管理员）时，
     * 更新主体必须是当前登录人，而不是 DTO 里客户端任意传的 username。
     */
    @Test
    void settingShouldUseLoginUserAsUpdateSubject() {
        // 构造当前登录会话：登录人是 admin
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute(GlobalConstant.SESSION_KEY, "admin");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setSession(mockSession);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
        try {
            AuthEditInfoDto dto = new AuthEditInfoDto();
            // 客户端伪造越权目标
            dto.setUsername("root");
            dto.setNickname("hacked");

            when(userDao.settingInfo(dto, "admin")).thenReturn(1);
            assertDoesNotThrow(() -> authService.setting(dto));
            // 更新主体必须是当前登录人 admin，而不是 dto 里伪造的 root
            verify(userDao).settingInfo(dto, "admin");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
