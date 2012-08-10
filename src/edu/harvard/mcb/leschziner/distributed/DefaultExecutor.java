package edu.harvard.mcb.leschziner.distributed;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultExecutor {
    public static ExecutorService getExecutor(String string) {
        // return Hazelcast.getExecutorService(string);
        return Executors.newCachedThreadPool();
    }
}
