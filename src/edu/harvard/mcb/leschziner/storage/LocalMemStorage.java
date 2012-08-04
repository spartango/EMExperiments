package edu.harvard.mcb.leschziner.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.event.BufferedQueue;
import edu.harvard.mcb.leschziner.storage.localstorage.EventBlockingQueue;
import edu.harvard.mcb.leschziner.storage.localstorage.HashMultiMap;
import edu.harvard.mcb.leschziner.storage.localstorage.LocalAtomicNumber;

public class LocalMemStorage implements StorageEngine {
    private final Map<String, Map>           maps;
    private final Map<String, BlockingQueue> queues;
    private final Map<String, BufferedQueue> bufferedQueues;
    private final Map<String, Set>           sets;
    private final Map<String, MultiMap>      multiMaps;
    private final Map<String, AtomicNumber>  atomicNumbers;

    public LocalMemStorage() {
        maps = new ConcurrentHashMap<String, Map>();
        queues = new ConcurrentHashMap<String, BlockingQueue>();
        bufferedQueues = new ConcurrentHashMap<String, BufferedQueue>();
        sets = new ConcurrentHashMap<String, Set>();
        multiMaps = new ConcurrentHashMap<String, MultiMap>();
        atomicNumbers = new ConcurrentHashMap<String, AtomicNumber>();
    }

    @Override public AtomicNumber getAtomicNumber(String name) {
        // Look the key up
        if (!atomicNumbers.containsKey(name)) {
            // If it doesnt exist, create a new one
            AtomicNumber newNumber = new LocalAtomicNumber(name);
            atomicNumbers.put(name, newNumber);
            return newNumber;
        } else {
            return atomicNumbers.get(name);
        }
    }

    @Override public <K, V> Map<K, V> getMap(String name) {
        // Look the key up
        if (!maps.containsKey(name)) {
            // If it doesnt exist, create a new one
            Map<K, V> newMap = new HashMap<>();
            maps.put(name, newMap);
            return newMap;
        } else {
            return maps.get(name);
        }
    }

    @Override public <K, V> MultiMap<K, V> getMultiMap(String name) {
        // Look the key up
        if (!multiMaps.containsKey(name)) {
            // If it doesnt exist, create a new one
            MultiMap<K, V> newMap = new HashMultiMap<>(name);
            multiMaps.put(name, newMap);
            return newMap;
        } else {
            return multiMaps.get(name);
        }
    }

    @Override public <T> BlockingQueue<T> getQueue(String name) {
        // Look the key up
        if (!queues.containsKey(name)) {
            // If it doesnt exist, create a new one
            BlockingQueue<T> newQueue = new EventBlockingQueue<>(name);
            queues.put(name, newQueue);
            return newQueue;
        } else {
            return queues.get(name);
        }
    }

    @Override public <T> Set<T> getSet(String name) {
        // Look the key up
        if (!sets.containsKey(name)) {
            // If it doesnt exist, create a new one
            Set<T> newSet = new HashSet<>();
            sets.put(name, newSet);
            return newSet;
        } else {
            return sets.get(name);
        }
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

    @Override public void destroyAtomicNumber(String name) {
        atomicNumbers.remove(name);
    }

    @Override public void destroyMap(String name) {
        maps.remove(name);
    }

    @Override public void destroyMultiMap(String name) {
        multiMaps.remove(name);
    }

    @Override public void destroyQueue(String name) {
        queues.remove(name);
    }

    @Override public void destroyBufferedQueue(String name) {
        bufferedQueues.remove(name);
    }

    @Override public void destroySet(String name) {
        sets.remove(name);
    }

}
