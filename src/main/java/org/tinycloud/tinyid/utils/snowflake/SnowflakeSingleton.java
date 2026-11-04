package org.tinycloud.tinyid.utils.snowflake;


import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 唯一ID工具-IdUtil
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-07-26 15:11:53
 */
public class SnowflakeSingleton {

    private static volatile long workerId = -1L;

    private static volatile long datacenterId = -1L;

    private static SnowflakeId snowflakeIdObj;

    public static synchronized void init(long workerId, long datacenterId) {
        if (snowflakeIdObj != null) {
            return;
        }
        SnowflakeSingleton.workerId = workerId;
        SnowflakeSingleton.datacenterId = datacenterId;
        snowflakeIdObj = new SnowflakeId(workerId, datacenterId);
    }

    public static long getWorkerId() {
        return workerId;
    }

    public static long getDatacenterId() {
        return datacenterId;
    }

    public static boolean isInitialized() {
        return snowflakeIdObj != null;
    }

    /**
     * 生成雪花id
     *
     * @return String
     */
    public static String nextId() {
        return String.valueOf(snowflakeIdObj.nextId());
    }

    /**
     * 生成雪花id
     *
     * @return id
     */
    public static long nextLongId() {
        return snowflakeIdObj.nextId();
    }


    /**
     * 批量生成雪花id
     *
     * @return id列表
     */
    public static List<Long> nextBatchLongId(int batchSize) {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            long id = snowflakeIdObj.nextId();
            idList.add(id);
        }
        return idList;
    }
}
