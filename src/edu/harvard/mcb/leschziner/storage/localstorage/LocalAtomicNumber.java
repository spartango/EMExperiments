package edu.harvard.mcb.leschziner.storage.localstorage;

import java.util.concurrent.atomic.AtomicLong;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.impl.monitor.LocalAtomicNumberStatsImpl;
import com.hazelcast.monitor.LocalAtomicNumberStats;

public class LocalAtomicNumber extends AtomicLong implements AtomicNumber {

    /**
     * 
     */
    private static final long serialVersionUID = -9222924095772896756L;

    private final String      name;

    public LocalAtomicNumber(String name) {
        super();
        this.name = name;
    }

    public LocalAtomicNumber(long initialValue, String name) {
        super(initialValue);
        this.name = name;
    }

    @Override public Object getId() {
        return hashCode();
    }

    @Override public InstanceType getInstanceType() {
        return InstanceType.ATOMIC_NUMBER;
    }

    @Override public LocalAtomicNumberStats getLocalAtomicNumberStats() {
        // This doesnt make much sense
        return new LocalAtomicNumberStatsImpl();
    }

    @Override public String getName() {
        return name;
    }

    @Override public void destroy() {
        // This doesn't make that much sense
    }

}
