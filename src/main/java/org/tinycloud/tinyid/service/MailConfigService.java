package org.tinycloud.tinyid.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.tinyid.bean.dto.MailConfigEditDto;
import org.tinycloud.tinyid.bean.entity.TMailConfig;
import org.tinycloud.tinyid.bean.vo.MailConfigVo;
import org.tinycloud.tinyid.dao.MailConfigDao;
import org.tinycloud.tinyid.utils.AuthUtils;
import org.tinycloud.tinyid.utils.BeanConvertUtils;
import org.tinycloud.tinyid.utils.EmailUtils;

/**
 * <p>
 * 邮件配置服务：维护 SMTP 告警配置，并向运维人员发送节点告警邮件
 * </p>
 *
 * @author liuxingyu01
 * @since 2026-11-04
 */
@Service
public class MailConfigService {
    private static final Logger logger = LoggerFactory.getLogger(MailConfigService.class);

    /**
     * 告警抑制窗口（毫秒）：窗口内同一类告警最多发一条，避免心跳每2分钟失败一次把邮箱刷爆
     */
    private static final long ALARM_SUPPRESS_MILLIS = 10 * 60 * 1000L;

    @Autowired
    private MailConfigDao mailConfigDao;

    /**
     * 最近一次告警时间戳，用于抑制窗口判断（心跳任务为单线程调度，此处仅做基础防护）
     */
    private volatile long lastAlarmAt = 0L;

    /**
     * 查询当前生效的邮件配置
     *
     * @return MailConfigVo，未配置时返回null
     */
    public MailConfigVo detail() {
        TMailConfig mailConfig = this.mailConfigDao.get();
        if (mailConfig == null) {
            return null;
        } else {
            return BeanConvertUtils.convertTo(mailConfig, MailConfigVo::new);
        }
    }

    /**
     * 保存邮件配置（系统中仅一条：有则更新，无则新增）
     *
     * @param dto 配置内容
     * @return 是否成功
     */
    public boolean edit(MailConfigEditDto dto) {
        TMailConfig mailConfig = this.mailConfigDao.get();
        int num;
        if (mailConfig != null) {
            // 更新已有配置
            mailConfig.setSmtpAddress(dto.getSmtpAddress());
            mailConfig.setSmtpPort(dto.getSmtpPort());
            mailConfig.setEmailAccount(dto.getEmailAccount());
            mailConfig.setEmailPassword(dto.getEmailPassword());
            mailConfig.setReceiveEmail(dto.getReceiveEmail());
            mailConfig.setSslFlag(dto.getSslFlag());
            num = this.mailConfigDao.update(mailConfig, (String) AuthUtils.getLoginId()) ? 1 : 0;
        } else {
            // 首次配置，插入
            mailConfig = new TMailConfig();
            mailConfig.setSmtpAddress(dto.getSmtpAddress());
            mailConfig.setSmtpPort(dto.getSmtpPort());
            mailConfig.setEmailAccount(dto.getEmailAccount());
            mailConfig.setEmailPassword(dto.getEmailPassword());
            mailConfig.setReceiveEmail(dto.getReceiveEmail());
            mailConfig.setSslFlag(dto.getSslFlag());
            num = this.mailConfigDao.insert(mailConfig, (String) AuthUtils.getLoginId()) ? 1 : 0;
        }
        return num > 0;
    }

    /**
     * 发送告警邮件（带抑制窗口：10分钟内最多发一条，窗口内重复告警直接跳过）
     *
     * @param title   邮件主题
     * @param content 邮件内容（支持html）
     */
    public void alarm(String title, String content) {
        if (!this.allowAlarm()) {
            logger.info("告警处于抑制窗口内，跳过发送: {}", title);
            return;
        }
        TMailConfig mailConfig = this.mailConfigDao.get();
        if (mailConfig == null) {
            // 未配置邮件时不告警，只留日志
            logger.warn("邮件配置不存在，告警邮件跳过: {}", title);
            return;
        }
        boolean result = EmailUtils.sendMsg(mailConfig.getEmailAccount(),
                mailConfig.getEmailPassword(),
                mailConfig.getSmtpAddress(),
                mailConfig.getSmtpPort(),
                "0".equals(mailConfig.getSslFlag()),
                mailConfig.getReceiveEmail().split(","),
                title,
                content);
        if (!result) {
            logger.error("告警邮件发送失败: {}", title);
        }
    }

    /**
     * 判断本次告警是否允许发送：抑制窗口内（10分钟）最多发一条。
     * 抑制状态为单节点内存态，多节点部署时最坏情况是每个节点每个窗口各一条，只会多报不会漏报。
     *
     * @return true 允许发送
     */
    private synchronized boolean allowAlarm() {
        long now = System.currentTimeMillis();
        if (now - this.lastAlarmAt >= ALARM_SUPPRESS_MILLIS) {
            this.lastAlarmAt = now;
            return true;
        }
        return false;
    }
}
