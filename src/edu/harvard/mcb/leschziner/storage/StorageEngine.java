package edu.harvard.mcb.leschziner.storage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.MultiMap;

import edu.harvard.mcb.leschziner.event.BufferedQueue;

public interface StorageEngine {

    public AtomicNumber getAtomicNumber(String name);

    public <K, V> Map<K, V> getMap(String name);

    public <K, V> MultiMap<K, V> getMultiMap(String name);

    public <T> BlockingQueue<T> getQueue(String name);

    public <T> BufferedQueue<T> getBufferedQueue(String name);

    public <T> Set<T> getSet(String name);

}
