package org.tinycloud.tinyid.bean.assist;

import lombok.Getter;
import org.tinycloud.tinyid.bean.entity.TIdTable;

/**
 * <p>
 * 单个流水号标识的内存号段游标。
 * </p>
 *
 * <p>号段区间为 (cursor, max]：每次取号把 cursor 加一，加到 max 时重新向
 * 数据库申请下一段。所有读写都在 synchronized (buffer) 内完成，
 * 跨实例则由数据库的原子 UPDATE + 行锁保证区间不重叠。</p>
 *
 * @author liuxingyu01
 * @since 2026-11-05
 */
@Getter
public class SegmentBuffer {

    /**
     * 本段已发出的最大值，初始为 0
     */
    private long cursor = 0L;

    /**
     * 本段可发出的最大值，初始为 0，首次取号时触发续段
     */
    private long max = 0L;

    /**
     * 最近一次续段时读回的配置快照，用于拼装号码
     */
    private TIdTable config;

    /**
     * 配置被修改或删除后置为 true，正在使用本缓冲的线程需丢弃并重建
     */
    private volatile boolean stale = false;

    /**
     * 判断当前号段是否已发完。
     *
     * @return true 表示需要向数据库申请下一段
     */
    public boolean exhausted() {
        return this.cursor >= this.max;
    }

    /**
     * 装载新的号段区间。
     *
     * @param start  新区间的起点（不含），通常是「新最大值 - 步长」
     * @param max    新区间的末端（含）
     * @param config 本次续段读回的配置
     */
    public void reset(long start, long max, TIdTable config) {
        this.cursor = start;
        this.max = max;
        this.config = config;
    }

    /**
     * 推进游标，发出本段的下一个数值。
     */
    public void next() {
        this.cursor++;
    }

    /**
     * 标记本缓冲已失效，阻止在途线程继续用旧配置发号。
     */
    public void markStale() {
        this.stale = true;
    }

    /**
     * 判断本缓冲是否已被标记为失效。
     *
     * @return true 表示应重建
     */
    public boolean isStale() {
        return this.stale;
    }
}
