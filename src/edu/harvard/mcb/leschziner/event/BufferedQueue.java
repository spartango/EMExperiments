package edu.harvard.mcb.leschziner.event;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.ICollection;
import com.hazelcast.core.ItemListener;

import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

public class BufferedQueue<T> implements
                              BlockingQueue<T>,
                              ICollection<T>,
                              Runnable {
    // The fast queue we'll absorb events into
    private final LinkedBlockingQueue<T> bufferQueue;

    // The slow queue we're buffering against
    private final BlockingQueue<T>       masterQueue;

    // Thread that grabs elements from the buffer and sends them off
    private final Thread                 transferThread;
    private boolean                      running;

    public BufferedQueue(String queueName) {
        masterQueue = DefaultStorageEngine.getStorageEngine()
                                          .getQueue(queueName);
        bufferQueue = new LinkedBlockingQueue<>();
        transferThread = new Thread(this);
        this.running = true;
        transferThread.start();

    }

    @Override public boolean add(T target) {
        return bufferQueue.add(target);
    }

    @Override public T poll() {
        return masterQueue.poll();
    }

    @Override public void addItemListener(ItemListener<T> arg0, boolean arg1) {
        ((ICollection<T>) masterQueue).addItemListener(arg0, arg1);
    }

    @Override public void removeItemListener(ItemListener<T> arg0) {
        ((ICollection<T>) masterQueue).removeItemListener(arg0);
    }

    @Override public boolean addAll(Collection<? extends T> c) {
        return bufferQueue.addAll(c);
    }

    @Override public int remainingCapacity() {
        return bufferQueue.remainingCapacity();
    }

    @Override public void put(T e) throws InterruptedException {
        bufferQueue.put(e);
    }

    @Override public boolean
            offer(T e, long timeout, TimeUnit unit) throws InterruptedException {
        return bufferQueue.offer(e, timeout, unit);
    }

    @Override public boolean offer(T e) {
        return bufferQueue.offer(e);
    }

    @Override public int size() {
        return masterQueue.size();
    }

    @Override public boolean isEmpty() {
        return masterQueue.isEmpty();
    }

    @Override public T remove() {
        return masterQueue.remove();
    }

    @Override public T element() {
        return masterQueue.element();
    }

    @Override public T peek() {
        return masterQueue.peek();
    }

    @Override public T take() throws InterruptedException {
        return masterQueue.take();
    }

    @Override public T
            poll(long timeout, TimeUnit unit) throws InterruptedException {
        return masterQueue.poll(timeout, unit);
    }

    @Override public boolean remove(Object o) {
        return masterQueue.remove(o);
    }

    @Override public boolean contains(Object o) {
        return masterQueue.contains(o);
    }

    @Override public int drainTo(Collection<? super T> c) {
        return masterQueue.drainTo(c);
    }

    @Override public boolean containsAll(Collection<?> c) {
        return masterQueue.containsAll(c);
    }

    @Override public int drainTo(Collection<? super T> c, int maxElements) {
        return masterQueue.drainTo(c, maxElements);
    }

    @Override public boolean removeAll(Collection<?> c) {
        return masterQueue.removeAll(c);
    }

    @Override public boolean retainAll(Collection<?> c) {
        return masterQueue.retainAll(c);
    }

    @Override public void clear() {
        masterQueue.clear();
    }

    @Override public void run() {
        while (running) {
            try {
                // Grab from the buffer
                T item = bufferQueue.take();
                // Pass it to the master
                masterQueue.add(item);
            } catch (InterruptedException e) {
                // Go around and check that we're still supposed to be
                // running
            }
        }
    }

    public void shutdown() {
        this.running = false;
        transferThread.interrupt();
    }

    @Override public Iterator<T> iterator() {
        return masterQueue.iterator();
    }

    @Override public Object[] toArray() {
        return masterQueue.toArray();
    }

    @Override public <T> T[] toArray(T[] a) {
        return masterQueue.toArray(a);
    }

    @Override public void destroy() {
        ((ICollection<T>) masterQueue).destroy();
    }

    @Override public Object getId() {
        return ((ICollection<T>) masterQueue).getId();
    }

    @Override public InstanceType getInstanceType() {
        return ((ICollection<T>) masterQueue).getInstanceType();
    }

    @Override public String getName() {
        return ((ICollection<T>) masterQueue).getName();
    }
}
