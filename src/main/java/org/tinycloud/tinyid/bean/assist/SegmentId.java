package org.tinycloud.tinyid.bean.assist;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * <p>
 * 号段缓冲模型：对应某一个 idCode 的「号段缓存」。
 * </p>
 *
 * <p>一个 idCode 在内存中只存在一个 {@code SegmentId} 实例，由
 * {@link org.tinycloud.tinyid.service.SegmentIdService} 里的缓存 Map 维护。</p>
 *
 * <p>工作方式（号段模式的核心）：</p>
 * <ul>
 *     <li>{@code queue} 缓存一批已从数据库预取的流水号，取号时直接从队列 poll，避免每次都打库；</li>
 *     <li>{@code step} 为号段步长，即每次从数据库预取的 ID 数量，也是触发预加载的基准；</li>
 *     <li>{@code preloaded} 标记当前是否已有「异步预加载下一号段」的任务在进行，避免并发重复预加载；</li>
 *     <li>{@code future} 持有预加载任务的句柄，队列空且正在预加载时，调用方靠它阻塞等待结果返回。</li>
 * </ul>
 *
 * @author liuxingyu01
 * @since 2024-04-30 09:47
 */
public final class SegmentId {

    /**
     * 号段缓存队列，存放已预取、待消费的流水号字符串
     */
    private volatile ConcurrentLinkedQueue<String> queue;

    /**
     * 号段步长：每次从数据库预取的 ID 数量，也是触发预加载的阈值基准
     */
    private Integer step;

    /**
     * 是否正在进行异步预加载下一号段（true=预加载任务进行中）
     */
    private volatile Boolean preloaded = false;

    /**
     * 异步预加载任务句柄，用于队列空时阻塞等待预加载结果
     */
    private volatile Future<?> future;

    /**
     * 构造一个号段缓冲实例
     *
     * @param queue 号段缓存队列
     * @param step  号段步长
     */
    public SegmentId(ConcurrentLinkedQueue<String> queue, Integer step) {
        this.queue = queue;
        this.step = step;
    }

    public ConcurrentLinkedQueue<String> getQueue() {
        return queue;
    }

    public void setQueue(ConcurrentLinkedQueue<String> queue) {
        this.queue = queue;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Boolean isPreloaded() {
        return preloaded;
    }

    public void setPreloaded(Boolean preloaded) {
        this.preloaded = preloaded;
    }

    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }
}
