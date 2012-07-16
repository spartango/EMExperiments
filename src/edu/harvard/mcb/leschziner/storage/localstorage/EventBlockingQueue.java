package edu.harvard.mcb.leschziner.storage.localstorage;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.hazelcast.core.ICollection;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemEventType;
import com.hazelcast.core.ItemListener;

public class EventBlockingQueue<E> extends LinkedBlockingQueue<E> implements
                                                                 ICollection<E> {

    /**
     * 
     */
    private static final long        serialVersionUID = -8333359858822876327L;

    private final String             name;
    private final List<ItemListener> listeners;

    public EventBlockingQueue(String name) {
        this.name = name;
        listeners = new LinkedList<ItemListener>();
    }

    @Override public void destroy() {
        // This isn't all that useful
        this.clear();
    }

    @Override public Object getId() {
        return hashCode();
    }

    @Override public InstanceType getInstanceType() {
        return InstanceType.QUEUE;
    }

    @Override public void addItemListener(ItemListener<E> arg0, boolean arg1) {
        listeners.add(arg0);
    }

    @Override public String getName() {
        return name;
    }

    @Override public void removeItemListener(ItemListener<E> arg0) {
        listeners.remove(arg0);
    }

    @Override public boolean add(E e) {
        if (super.add(e)) {
            // Notify listeners
            notifyListenersOfAdd(e);
            return true;
        }
        return false;
    }

    @Override public boolean addAll(Collection<? extends E> c) {
        if (super.addAll(c)) {
            // Notify listeners
            notifyListenersOfAdd(c);
            return true;
        }
        return false;
    }

    private void notifyListenersOfAdd(E added) {
        for (ItemListener<E> listener : listeners) {
            listener.itemAdded(new ItemEvent<E>("q:" + name,
                                                ItemEventType.ADDED,
                                                added));
        }
    }

    private void notifyListenersOfAdd(Collection<? extends E> added) {
        for (E add : added) {
            notifyListenersOfAdd(add);
        }
    }

}
