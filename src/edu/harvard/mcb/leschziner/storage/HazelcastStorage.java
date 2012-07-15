package edu.harvard.mcb.leschziner.storage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

public class HazelcastStorage implements StorageEngine {
    private final HazelcastInstance instance;

    public HazelcastStorage() {
        this(Hazelcast.getDefaultInstance());
    }

    public HazelcastStorage(HazelcastInstance instance) {
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

}
