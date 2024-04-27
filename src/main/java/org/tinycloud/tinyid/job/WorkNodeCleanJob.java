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
 *     定时删除处于非活跃状态的节点（signed_at时间在一天前的记录）
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 15:34
 */
@Slf4j
@Component
public class WorkNodeCleanJob {

    @Autowired
    private WorkNodeDao workNodeDao;

    /**
     * 延迟120秒，后续每1小时执行一次
     */
    @Scheduled(initialDelay = 1000 * 120, fixedDelay = 1000 * 3600)
    public void scheduled() {
        log.info("WorkNodeClean job start in {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        workNodeDao.clean();

        log.info("WorkNodeClean job end in {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
