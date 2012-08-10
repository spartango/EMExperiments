package edu.harvard.mcb.leschziner.storage.localstorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MultiMap;

public class HashMultiMap<K, V> implements MultiMap<K, V> {

    private final Map<K, Collection<V>>                   multimap;

    private final Collection<EntryListener<K, V>>         globalListeners;
    private final Map<K, Collection<EntryListener<K, V>>> specificListeners;

    private final String                                  name;

    public HashMultiMap(String name) {
        multimap = new ConcurrentHashMap<>();
        globalListeners = new ConcurrentLinkedQueue<>();
        specificListeners = new ConcurrentHashMap<>();
        this.name = name;
    }

    @Override public void destroy() {
        this.clear();
    }

    @Override public Object getId() {
        return hashCode();
    }

    @Override public InstanceType getInstanceType() {
        return InstanceType.MULTIMAP;
    }

    @Override public void addEntryListener(EntryListener<K, V> listener,
                                           boolean arg1) {
        globalListeners.add(listener);
    }

    @Override public void addEntryListener(EntryListener<K, V> listener,
                                           K key,
                                           boolean arg2) {
        if (specificListeners.containsKey(key)) {
            specificListeners.get(key).add(listener);
        } else {
            Collection<EntryListener<K, V>> keyListeners = new ConcurrentLinkedQueue<>();
            keyListeners.add(listener);
            specificListeners.put(key, keyListeners);
        }

    }

    @Override public void addLocalEntryListener(EntryListener<K, V> listener) {
        globalListeners.add(listener);
    }

    @Override public void clear() {
        multimap.clear();
        globalListeners.clear();
        specificListeners.clear();
    }

    @Override public boolean containsEntry(K key, V value) {
        return multimap.containsKey(key) && multimap.get(key).contains(value);
    }

    @Override public boolean containsKey(K key) {
        return multimap.containsKey(key);
    }

    @Override public boolean containsValue(Object arg0) {
        for (Collection<V> values : multimap.values()) {
            if (values.contains(arg0)) {
                return true;
            }
        }
        return false;
    }

    @Override public Set<java.util.Map.Entry<K, V>> entrySet() {
        HashSet<java.util.Map.Entry<K, V>> entrySet = new HashSet<>();
        // For each key
        for (K key : multimap.keySet()) {
            // Associate key with each value
            for (V value : multimap.get(key)) {
                entrySet.add(new Entry<K, V>(key, value));
            }
        }

        return entrySet;
    }

    @Override public Collection<V> get(K key) {
        return multimap.get(key);
    }

    @Override public String getName() {
        return name;
    }

    @Override public Set<K> keySet() {
        return multimap.keySet();
    }

    @Override public Set<K> localKeySet() {
        return multimap.keySet();
    }

    @Override public void lock(K arg0) {
        // Using concurrent datastructures, this is not necessary
    }

    @Override public boolean lockMap(long arg0, TimeUnit arg1) {
        // Using concurrent datastructures, this is not necessary
        return true;
    }

    @Override public boolean put(K key, V value) {
        if (multimap.containsKey(key) && multimap.get(key).add(value)) {
            notifyListenersOfAdd(key, value);
        } else {
            ConcurrentLinkedQueue<V> list = new ConcurrentLinkedQueue<>();
            list.add(value);
            multimap.put(key, list);
            notifyListenersOfAdd(key, value);
        }
        return true;
    }

    @Override public Collection<V> remove(Object key) {
        Collection<V> values = multimap.remove(key);
        // Notify listeners
        notifyListenersOfRemove((K) key, null);
        return values;
    }

    @Override public boolean remove(Object key, Object value) {
        if (multimap.containsKey(key) && multimap.get(key).remove(value)) {
            // Notify Listeners
            notifyListenersOfRemove((K) key, (V) value);
            return true;
        }
        return false;

    }

    @Override public void removeEntryListener(EntryListener<K, V> arg0) {
        globalListeners.remove(arg0);
    }

    @Override public void removeEntryListener(EntryListener<K, V> listener,
                                              K key) {
        if (specificListeners.containsKey(key))
            specificListeners.get(key).remove(listener);
    }

    @Override public int size() {
        return multimap.size();
    }

    @Override public boolean tryLock(K arg0) {
        // Using concurrent datastructures, this is not necessary
        return true;
    }

    @Override public boolean tryLock(K arg0, long arg1, TimeUnit arg2) {
        // Using concurrent datastructures, this is not necessary
        return true;
    }

    @Override public void unlock(K arg0) {
        // Using concurrent datastructures, this is not necessary
    }

    @Override public void unlockMap() {
        // Using concurrent datastructures, this is not necessary
    }

    @Override public int valueCount(K arg0) {
        int count = 0;
        for (Collection<V> keyValues : multimap.values()) {
            count += keyValues.size();
        }
        return count;
    }

    @Override public Collection<V> values() {
        List<V> values = new LinkedList<>();
        for (Collection<V> keyValues : multimap.values()) {
            values.addAll(keyValues);
        }
        return values();
    }

    private void notifyListenersOfAdd(K key, V value) {
        // Notify All
        EntryEvent<K, V> event = new EntryEvent<K, V>("m:" + name,
                                                      null,
                                                      EntryEvent.TYPE_ADDED,
                                                      key,
                                                      value);
        for (EntryListener<K, V> listener : globalListeners) {
            listener.entryAdded(event);
        }
        if (specificListeners.containsKey(key))
            for (EntryListener<K, V> listener : specificListeners.get(key)) {
                listener.entryAdded(event);
            }
    }

    private void notifyListenersOfRemove(K key, V value) {
        // Notify All
        EntryEvent<K, V> event = new EntryEvent<K, V>("m:" + name,
                                                      null,
                                                      EntryEvent.TYPE_REMOVED,
                                                      key,
                                                      value);
        for (EntryListener<K, V> listener : globalListeners) {
            listener.entryRemoved(event);
        }
        // Notify Specific
        if (specificListeners.containsKey(key))
            for (EntryListener<K, V> listener : specificListeners.get(key)) {
                listener.entryRemoved(event);
            }
    }

    static class Entry<K, V> implements java.util.Map.Entry<K, V> {
        final K key;
        V       value;

        /**
         * Creates new entry.
         */
        Entry(K k, V v) {
            value = v;
            key = k;
        }

        @Override public final K getKey() {
            return key;
        }

        @Override public final V getValue() {
            return value;
        }

        @Override public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }
    }
}
