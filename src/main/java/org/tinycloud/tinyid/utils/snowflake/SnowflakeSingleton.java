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

    public static long workerId;

    public static long datacenterId;

    private static Snowflake snowflakeObj;

    public static void init(long workerId, long datacenterId) {
        SnowflakeSingleton.workerId = workerId;
        SnowflakeSingleton.datacenterId = datacenterId;
        snowflakeObj = new Snowflake(workerId, datacenterId);
    }

    /**
     * 生成雪花id
     *
     * @return String
     */
    public static String nextId() {
        return String.valueOf(snowflakeObj.nextId());
    }

    /**
     * 生成雪花id
     *
     * @return id
     */
    public static long nextLongId() {
        return snowflakeObj.nextId();
    }


    /**
     * 批量生成雪花id
     *
     * @return id列表
     */
    public static List<Long> nextBatchLongId(int batchSize) {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            long id = snowflakeObj.nextId();
            idList.add(id);
        }
        return idList;
    }
}
