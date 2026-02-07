package com.database.cleanup;

import com.database.core.InMemoryDatabase;

public class CleanupTask implements Runnable {
    private final InMemoryDatabase<?> database;
    private final long intervalMillis;
    private volatile boolean active;

    public CleanupTask(InMemoryDatabase<?> database, long intervalMillis) {
        this.database = database;
        this.intervalMillis = intervalMillis;
        this.active = true;
    }

    @Override
    public void run() {
        while (active && database.isRunning()) {
            try {
                database.cleanupExpiredKeys();
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        active = false;
    }
}