package org.tinycloud.tinyid.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tinycloud.tinyid.dao.WorkNodeDao;

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

    /**
     * 延迟60秒，后续每120秒执行一次
     */
    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 120)
    public void scheduled() {
        log.info("WorkNodeRefresh job start in {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        workNodeDao.sign();

        log.info("WorkNodeRefresh job end in {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
