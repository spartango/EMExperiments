package edu.harvard.mcb.leschziner.storage;

public class DefaultStorageEngine {
    private static StorageEngine defaultEngine = new LocalMemStorage();

    public static StorageEngine getStorageEngine() {
        return defaultEngine;
    }
}
