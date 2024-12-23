package org.tinycloud.tinyid.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.tinycloud.tinyid.bean.entity.TWorkNode;
import org.tinycloud.tinyid.dao.WorkNodeDao;
import org.tinycloud.tinyid.utils.LocalHostUtils;
import org.tinycloud.tinyid.utils.snowflake.SnowflakeSingleton;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * <p>
 * 雪花id启动时执行类，自动生成未被使用的datacenterId和workerId
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 10:17
 */
@ConditionalOnProperty(prefix = "tinyid", name = "snowflake-loader", havingValue = "SnowflakeApplicationRunner")
@Component
@Order(98)
public class SnowflakeApplicationRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeApplicationRunner.class);

    private InetAddress inetAddress;

    @Autowired
    private WorkNodeDao workNodeDao;

    @Autowired
    private Environment environment;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Initialization snowflake start!");
        Long count = this.workNodeDao.getDistinctDatacenterIds();
        if (count >= 32L * 32L) {
            throw new IllegalStateException("datacenterId was used up, aborting application startup!");
        }
        this.inetAddress = LocalHostUtils.getInetAddress();
        String hostName = this.inetAddress.getHostName();
        String hostAddress = this.inetAddress.getHostAddress();
        logger.info("Initialization snowflake network:" + hostName + "/" + hostAddress);

        // 第一步、首次先根据网卡信息生成datacenterId
        long datacenterId = this.getDatacenterId(31L);
        long workerId;
        List<TWorkNode> nodeList = null;
        boolean condition = true;
        while (condition) {
            nodeList = this.workNodeDao.getWorkNodeListByDatacenterId(datacenterId);
            if (nodeList.size() >= 32) { // 表示此数据中心下机器号已用完，则重新随机数据中心ID
                datacenterId = getRandomId();
            } else {
                List<Long> workerIdList = nodeList.stream().map(TWorkNode::getWorkerId).collect(Collectors.toList());
                workerId = getRandomId();
                if (!workerIdList.contains(workerId)) {
                    try {
                        // 第二步、插入数据库，能插成功就行，
                        this.workNodeDao.addWorkNode(hostAddress, environment.getProperty("server.port", Integer.class), workerId, datacenterId);

                        // 第三步、加载全局Snowflake对象
                        SnowflakeSingleton.init(workerId, datacenterId);
                        condition = false;
                    } catch (Exception e) {
                        if (e instanceof DuplicateKeyException) {
                            logger.error("DuplicateKeyException: ", e);
                        } else {
                            logger.error("Other Exception : ", e);
                            throw e;
                        }
                    }
                }
            }
        }
        logger.info("Initialization snowflake end!");
    }


    /**
     * 数据标识id部分
     */
    private long getDatacenterId(long maxDatacenterId) {
        long id = 0L;
        try {
            if (this.inetAddress == null) {
                this.inetAddress = InetAddress.getLocalHost();
            }
            NetworkInterface network = NetworkInterface.getByInetAddress(this.inetAddress);
            if (null == network) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    id = ((0x000000FF & (long) mac[mac.length - 2]) | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
                    id = id % (maxDatacenterId + 1);
                }
            }
        } catch (Exception e) {
            logger.warn(" getDatacenterId error: ", e);
        }
        return id;
    }

    /**
     * 生成0-31之间的随机数，作为workerId或者datacenterId
     *
     * @return long
     */
    private static long getRandomId() {
        int max = 31;
        int min = 0;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
