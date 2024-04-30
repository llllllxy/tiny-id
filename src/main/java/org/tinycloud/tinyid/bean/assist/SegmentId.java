package org.tinycloud.tinyid.bean.assist;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-30 09:47
 */
public final class SegmentId {

    private volatile ConcurrentLinkedQueue<String> queue;

    private Integer step;

    private volatile Boolean preloaded = false;

    private volatile Future<?> future;

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

    public Boolean getPreloaded() {
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
