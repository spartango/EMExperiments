package edu.harvard.mcb.leschziner.storage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.event.BufferedQueue;

public class HazelcastStorage implements StorageEngine {
    private final HazelcastInstance          instance;
    private final Map<String, BufferedQueue> bufferedQueues;

    public HazelcastStorage() {
        bufferedQueues = new ConcurrentHashMap<String, BufferedQueue>();
        this.instance = Hazelcast.getDefaultInstance();
    }

    public HazelcastStorage(HazelcastInstance instance) {
        bufferedQueues = new ConcurrentHashMap<String, BufferedQueue>();
        this.instance = instance;
    }

    @Override public AtomicNumber getAtomicNumber(String name) {
        return instance.getAtomicNumber(name);
    }

    @Override public <K, V> Map<K, V> getMap(String name) {
        return instance.getMap(name);
    }

    @Override public <K, V> MultiMap<K, V> getMultiMap(String name) {
        return instance.getMultiMap(name);
    }

    @Override public <T> BlockingQueue<T> getQueue(String name) {
        return instance.getQueue(name);
    }

    @Override public <T> Set<T> getSet(String name) {
        return instance.getSet(name);
    }

    @Override public <T> BufferedQueue<T> getBufferedQueue(String name) {
        // Look the key up
        if (!bufferedQueues.containsKey(name)) {
            // If it doesnt exist, create a new one
            BufferedQueue<T> newQueue = new BufferedQueue<>(name);
            bufferedQueues.put(name, newQueue);
            return newQueue;
        } else {
            return bufferedQueues.get(name);
        }
    }
}
