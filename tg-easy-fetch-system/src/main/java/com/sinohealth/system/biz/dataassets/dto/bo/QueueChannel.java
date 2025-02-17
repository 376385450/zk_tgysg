package com.sinohealth.system.biz.dataassets.dto.bo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-07 15:39
 */
public class QueueChannel<E> {

    private final BlockingQueue<E> queue;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    public static QueueChannel<LinkedHashMap<String, Object>> buildExportChannel(int capacity) {
        ArrayBlockingQueue<LinkedHashMap<String, Object>> queue = new ArrayBlockingQueue<>(capacity);
        return new QueueChannel<>(queue);
    }


    public QueueChannel(BlockingQueue<E> queue) {
        this.queue = queue;
    }

    public void stop() {
        this.stop.set(true);
    }

    public boolean isRunning() {
        return !this.stop.get();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public int drainTo(Collection<? super E> c) {
        return queue.drainTo(c);
    }

    public void put(E e) throws InterruptedException {
        queue.put(e);
    }

    public boolean add(E e) {
        return queue.add(e);
    }
}
