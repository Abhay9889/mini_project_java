package com.database.core;

public class Entry<T> {
    private final T value;
    private final long expiryTime;

    public Entry(T value) {
        this.value = value;
        this.expiryTime = -1;
    }

    public Entry(T value, long ttlMillis) {
        this.value = value;
        this.expiryTime = System.currentTimeMillis() + ttlMillis;
    }

    public T getValue() {
        return value;
    }

    public boolean isExpired() {
        if (expiryTime == -1) {
            return false;
        }
        return System.currentTimeMillis() > expiryTime;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}