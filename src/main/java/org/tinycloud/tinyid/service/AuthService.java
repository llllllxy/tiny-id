package org.tinycloud.tinyid.service;


import jakarta.servlet.http.HttpSession;
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

    @Autowired
    private UserDao userDao;

    public CaptchaCodeVo getCode(HttpSession session) {
        // 保存验证码信息，生成验证key
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");

        String codeSessionKey = GlobalConstant.CAPTCHA_CODE_SESSION_KEY + uuid;

        // 生成验证码图片，并返回base64编码
        CaptchaCodeGen captchaCode = new CaptchaCodeGen(120, 38, 4, 10);
        String base64 = captchaCode.getBase64();
        // 生成4位随机数，作为验证码图片里的数
        String code = captchaCode.getCode();

        // 将验证码和私钥，存入redis 60秒
        session.setAttribute(codeSessionKey, code);

        // 验证码图片的bae64和rsa公钥返回给前端
        CaptchaCodeVo vo = new CaptchaCodeVo();
        vo.setImg(base64);
        vo.setUuid(uuid);
        return vo;
    }

    public void authentication(AuthLoginDto authLoginDto) {
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
        int rows = this.userDao.settingInfo(dto);
        return rows > 0;
    }
}
