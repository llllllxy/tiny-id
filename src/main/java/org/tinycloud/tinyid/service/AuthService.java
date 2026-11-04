package org.tinycloud.tinyid.service;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.tinyid.bean.dto.AuthEditInfoDto;
import org.tinycloud.tinyid.bean.dto.AuthEditPasswordDto;
import org.tinycloud.tinyid.bean.dto.AuthLoginDto;

import org.tinycloud.tinyid.bean.entity.TUser;
import org.tinycloud.tinyid.bean.vo.CaptchaCodeVo;
import org.tinycloud.tinyid.bean.vo.UserInfoVo;
import org.tinycloud.tinyid.constant.GlobalConstant;
import org.tinycloud.tinyid.dao.UserDao;
import org.tinycloud.tinyid.enums.CoreErrorCode;
import org.tinycloud.tinyid.exception.CoreException;
import org.tinycloud.tinyid.utils.AuthUtils;
import org.tinycloud.tinyid.utils.BeanConvertUtils;
import org.tinycloud.tinyid.utils.CaptchaCodeGen;
import org.tinycloud.tinyid.utils.secure.BCrypt;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 * 会话服务层
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-06-02 14:56
 */
@Service
public class AuthService {

    private static final String SALT = "323@#@$1234da323@#@$1234da";

    /**
     * 图形验证码本地缓存：key为captchaCode_<uuid>，5分钟自动过期，容量上限兜底防膨胀。
     * 验证码脱离session独立存在，登录页每次打开不再产生session记录。
     * 注意：本地缓存不能跨节点校验，多实例部署需会话粘滞或更换为集中式缓存。
     */
    private final Cache<String, String> captchaCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(10_000)
            .build();

    @Autowired
    private UserDao userDao;

    public CaptchaCodeVo getCode() {
        // 生成验证key
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");

        String cacheKey = GlobalConstant.CAPTCHA_CODE_SESSION_KEY + uuid;

        // 生成验证码图片，并返回base64编码
        CaptchaCodeGen captchaCode = new CaptchaCodeGen(120, 38, 4, 10);
        String base64 = captchaCode.getBase64();
        // 生成4位随机数，作为验证码图片里的数
        String code = captchaCode.getCode();

        // 验证码存入本地缓存，5分钟自动过期
        this.captchaCache.put(cacheKey, code);

        // 验证码图片的base64和验证key返回给前端
        CaptchaCodeVo vo = new CaptchaCodeVo();
        vo.setImg(base64);
        vo.setUuid(uuid);
        return vo;
    }

    public void authentication(AuthLoginDto authLoginDto) {
        // 校验图形验证码：先取后删，一次性消费，防止同一验证码重放爆破
        String captchaKey = GlobalConstant.CAPTCHA_CODE_SESSION_KEY + authLoginDto.getCaptchaKey();
        String cacheCode = this.captchaCache.getIfPresent(captchaKey);
        this.captchaCache.invalidate(captchaKey);
        // 验证码字符含大小写，忽略大小写比对以降低输入难度
        if (cacheCode == null || !cacheCode.equalsIgnoreCase(authLoginDto.getCaptchaCode())) {
            throw new CoreException(CoreErrorCode.THE_CAPTCHA_CODE_IS_INVALID);
        }

        String username = authLoginDto.getUsername();
        String password = authLoginDto.getPassword();

        TUser entity = this.userDao.getUserByUsername(username);
        // 用户不存在
        if (Objects.isNull(entity)) {
            throw new CoreException(CoreErrorCode.USERNAME_OR_PASSWORD_MISMATCH);
        }
        // 密码错误
        String encodedPassword = entity.getPassword();
        boolean checkResult = BCrypt.checkpw(password + SALT, encodedPassword);
        if (!checkResult) {
            throw new CoreException(CoreErrorCode.USERNAME_OR_PASSWORD_MISMATCH);
        }
        // 用户已被停用
        if (entity.getStatus().equals(GlobalConstant.DISABLED)) {
            throw new CoreException(CoreErrorCode.USER_IS_DISABLED);
        }
    }


    public UserInfoVo getUserInfo() {
        String username = (String) AuthUtils.getLoginId();
        TUser entity = this.userDao.getUserByUsername(username);
        if (entity == null) {
            return null;
        } else {
            return BeanConvertUtils.convertTo(entity, UserInfoVo::new);
        }
    }

    public boolean editPassword(AuthEditPasswordDto dto) {
        if (!dto.getNewPassword().equals(dto.getAgainPassword())) {
            throw new CoreException(CoreErrorCode.THE_NEWPASSWORD_ENTERED_TWICE_DOES_NOT_MATCH);
        }
        String username = (String) AuthUtils.getLoginId();
        TUser entity = this.userDao.getUserByUsername(username);
        String encodedPassword = entity.getPassword();
        boolean checkResult = BCrypt.checkpw(dto.getOldPassword() + SALT, encodedPassword);
        // 原始密码错误
        if (!checkResult) {
            throw new CoreException(CoreErrorCode.THE_ORIGINAL_PASSWORD_IS_WRONG);
        }
        String encodedNewPassword = BCrypt.hashpw(dto.getNewPassword() + SALT, BCrypt.gensalt());
        int rows = this.userDao.updatePassword(username, encodedNewPassword);
        return rows > 0;
    }


    public boolean setting(AuthEditInfoDto dto) {
        // 防水平越权：更新主体强制取当前登录人，客户端传入的 username 不可信，一律忽略
        String username = (String) AuthUtils.getLoginId();
        int rows = this.userDao.settingInfo(dto, username);
        return rows > 0;
    }
}
