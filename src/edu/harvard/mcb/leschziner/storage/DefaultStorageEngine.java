package edu.harvard.mcb.leschziner.storage;

public class DefaultStorageEngine {
    private static StorageEngine defaultEngine = new HazelcastStorage();

    public static StorageEngine getStorageEngine() {
        return defaultEngine;
    }
}
