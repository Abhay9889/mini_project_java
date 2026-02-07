package com.database.core;

import com.database.exception.DatabaseStoppedException;
import com.database.exception.KeyNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class InMemoryDatabase<T> {
    private final Map<Integer, Entry<T>> storage;
    private volatile boolean running;

    public InMemoryDatabase() {
        this.storage = new HashMap<>();
        this.running = true;
    }

    public synchronized void put(Integer key, T value) {
        checkIfRunning();
        storage.put(key, new Entry<>(value));
    }

    public synchronized void put(Integer key, T value, long ttlMillis) {
        checkIfRunning();
        storage.put(key, new Entry<>(value, ttlMillis));
    }

    public synchronized T get(Integer key) {
        Entry<T> entry = storage.get(key);
        
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            storage.remove(key);
            return null;
        }

        return entry.getValue();
    }

    public synchronized void delete(Integer key) {
        checkIfRunning();
        if (!storage.containsKey(key)) {
            throw new KeyNotFoundException("Key " + key + " not found");
        }
        storage.remove(key);
    }

    public synchronized void stop() {
        running = false;
    }

    public synchronized void start() {
        running = true;
    }

    public boolean isRunning() {
        return running;
    }

    public synchronized void cleanupExpiredKeys() {
        Iterator<Map.Entry<Integer, Entry<T>>> iterator = storage.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Integer, Entry<T>> mapEntry = iterator.next();
            if (mapEntry.getValue().isExpired()) {
                iterator.remove();
            }
        }
    }

    public synchronized int size() {
        return storage.size();
    }

    private void checkIfRunning() {
        if (!running) {
            throw new DatabaseStoppedException("Database is currently stopped");
        }
    }
}