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
 *     <li>{@code preloadFuture} 持有「异步预加载下一号段」任务的句柄：非空且未完成即视为预加载中，
 *     用于避免并发重复预加载；队列空时，调用方靠它阻塞等待预加载结果。</li>
 * </ul>
 *
 * @author liuxingyu01
 * @since 2024-04-30 09:47
 */
public final class SegmentId {

    /**
     * 号段缓存队列，存放已预取、待消费的流水号字符串
     */
    private final ConcurrentLinkedQueue<String> queue;

    /**
     * 号段步长：每次从数据库预取的 ID 数量，也是触发预加载的阈值基准
     */
    private final Integer step;

    /**
     * 异步预加载任务的句柄。以 {@code isDone()} 作为「是否有预加载任务进行中」的唯一事实来源：
     * 非空且未完成即预加载中；任务无论成功或失败，完成即视为结束。
     * 相比独立的布尔标志位，不存在标志与任务真实状态不同步的竞态。
     */
    private volatile Future<?> preloadFuture;

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

    public Integer getStep() {
        return step;
    }

    public Future<?> getPreloadFuture() {
        return preloadFuture;
    }

    public void setPreloadFuture(Future<?> preloadFuture) {
        this.preloadFuture = preloadFuture;
    }
}
