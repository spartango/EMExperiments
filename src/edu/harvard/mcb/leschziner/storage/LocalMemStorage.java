package edu.harvard.mcb.leschziner.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.storage.localstorage.HashMultiMap;
import edu.harvard.mcb.leschziner.storage.localstorage.LocalAtomicNumber;

public class LocalMemStorage implements StorageEngine {
    private Map<String, Map>           maps;
    private Map<String, BlockingQueue> queues;
    private Map<String, Set>           sets;
    private Map<String, MultiMap>      multiMaps;
    private Map<String, AtomicNumber>  atomicNumbers;

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
            MultiMap<K, V> newMap = new HashMultiMap<>();
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
            BlockingQueue<T> newQueue = new LinkedBlockingQueue<>();
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

}
