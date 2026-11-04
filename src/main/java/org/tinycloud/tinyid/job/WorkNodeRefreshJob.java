package org.tinycloud.tinyid.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tinycloud.tinyid.dao.WorkNodeDao;
import org.tinycloud.tinyid.service.MailConfigService;
import org.tinycloud.tinyid.utils.snowflake.SnowflakeSingleton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 定时刷新WorkNode节点状态
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 15:24
 */
@Slf4j
@Component
public class WorkNodeRefreshJob {

    @Autowired
    private WorkNodeDao workNodeDao;

    @Autowired
    private MailConfigService mailConfigService;

    /**
     * 延迟60秒，后续每120秒执行一次
     */
    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 120)
    public void scheduled() {
        log.info("WorkNodeRefresh job start in {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 心跳失败（含重新注册失败）必须留痕并邮件告警：静默失联会导致 workerID 被新实例复用，产生重复雪花ID
        boolean signed;
        try {
            signed = workNodeDao.sign();
        } catch (Exception e) {
            // DB 断连等异常：此时大概率也读不到邮件配置，告警会自动跳过，不阻断调度
            log.error("WorkNodeRefresh job: 节点心跳异常", e);
            signed = false;
        }
        if (!signed) {
            log.error("WorkNodeRefresh job: 节点心跳失败，请检查数据库连接及节点注册状态");
            String title = "TinyID-雪花节点心跳告警";
            String content = "<h2>雪花节点心跳失败！</h2>"
                    + "datacenterId：" + SnowflakeSingleton.getDatacenterId() + "<br/>"
                    + "workerId：" + SnowflakeSingleton.getWorkerId() + "<br/>"
                    + "告警时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "<br/>"
                    + "说明：节点心跳失败，若心跳行已被清理且重新注册被拒绝，存在雪花ID重复风险，请立即检查。";
            this.mailConfigService.alarm(title, content);
        }

        log.info("WorkNodeRefresh job end in {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
