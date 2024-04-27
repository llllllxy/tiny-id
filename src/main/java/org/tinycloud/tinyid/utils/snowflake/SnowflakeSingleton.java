package org.tinycloud.tinyid.utils.snowflake;


/**
 * <p>
 * 唯一ID工具-IdUtil
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-07-26 15:11:53
 */
public class SnowflakeSingleton {

    private static Snowflake snowflakeObj;

    public static void init(long workerId, long datacenterId) {
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
     * @return long
     */
    public static long nextLongId() {
        return snowflakeObj.nextId();
    }
}
