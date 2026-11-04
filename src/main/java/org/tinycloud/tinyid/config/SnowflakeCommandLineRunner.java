package org.tinycloud.tinyid.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.tinycloud.tinyid.dao.WorkNodeDao;
import org.tinycloud.tinyid.utils.IdExtractor;
import org.tinycloud.tinyid.utils.LocalHostUtils;
import org.tinycloud.tinyid.utils.snowflake.SnowflakeSingleton;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 *     雪花id启动时执行类，自动生成未被使用的datacenterId和workerId，一种新的方法，根据总ID求余计算datacenterId和workerId，性能更高
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-12-19 18:12
 */
@ConditionalOnProperty(prefix = "tinyid", name = "snowflake-loader", havingValue = "SnowflakeCommandLineRunner")
@Component
@Order(99)
public class SnowflakeCommandLineRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeCommandLineRunner.class);

    /**
     * 节点组合分配的最大重试次数，超过后直接失败终止启动，避免无限循环空转
     */
    private static final int MAX_ASSIGN_RETRY = 100;

    @Autowired
    private WorkNodeDao workNodeDao;

    @Autowired
    private Environment environment;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initialization snowflake start!");
        Long count = this.workNodeDao.getUsedTotalIdCount();
        if (count >= 32L * 32L) {
            throw new IllegalStateException("datacenterId was used up, aborting application startup!");
        }
        InetAddress inetAddress = LocalHostUtils.getInetAddress();
        String hostName = inetAddress.getHostName();
        String hostAddress = inetAddress.getHostAddress();
        logger.info("Initialization snowflake network:" + hostName + "/" + hostAddress);

        List<Long> totalIdList = this.workNodeDao.getTotalIds();

        logger.info("Initialization snowflake totalIdList:" + totalIdList);

        boolean assigned = false;
        int retryCount = 0;
        while (!assigned) {
            long nextTotalId;
            do {
                // 重新获取
                nextTotalId = ThreadLocalRandom.current().nextLong(IdExtractor.MAX_TOTAL_ID + 1);
            } while (totalIdList.contains(nextTotalId));

            long datacenterId = IdExtractor.extractDatacenterId((int) nextTotalId);
            long workerId = IdExtractor.extractWorkerId((int) nextTotalId);

            try {
                // 第二步、插入数据库，能插成功就行，
                this.workNodeDao.addWorkNode(hostAddress, environment.getProperty("server.port", Integer.class), workerId, datacenterId);
                // 第三步、加载全局Snowflake对象
                SnowflakeSingleton.init(workerId, datacenterId);
                assigned = true;
            } catch (DuplicateKeyException e) {
                // 唯一索引冲突：该组合已被其它节点抢注，刷新已占用集合后重试
                logger.warn("DuplicateKeyException, retry assigning snowflake node: {}", e.getMessage());
                totalIdList.add(nextTotalId);
                retryCount++;
            } catch (Exception e) {
                logger.error("Other Exception : ", e);
                throw e;
            }

            if (!assigned && retryCount >= MAX_ASSIGN_RETRY) {
                throw new IllegalStateException("Failed to assign snowflake node after " + MAX_ASSIGN_RETRY + " retries, aborting application startup!");
            }
        }
        logger.info("Initialization snowflake end!");
    }
}
