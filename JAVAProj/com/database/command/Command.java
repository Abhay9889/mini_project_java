package com.database.command;

public class Command {
    private final CommandType type;
    private final Integer key;
    private final String rawValue;
    private final Long ttl;

    public Command(CommandType type) {
        this(type, null, null, null);
    }

    public Command(CommandType type, Integer key) {
        this(type, key, null, null);
    }

    public Command(CommandType type, Integer key, String rawValue) {
        this(type, key, rawValue, null);
    }

    public Command(CommandType type, Integer key, String rawValue, Long ttl) {
        this.type = type;
        this.key = key;
        this.rawValue = rawValue;
        this.ttl = ttl;
    }

    public CommandType getType() {
        return type;
    }

    public Integer getKey() {
        return key;
    }

    public String getRawValue() {
        return rawValue;
    }

    public Long getTtl() {
        return ttl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        if (key != null) {
            sb.append(" ").append(key);
        }
        if (rawValue != null) {
            sb.append(" ").append(rawValue);
        }
        if (ttl != null) {
            sb.append(" ").append(ttl);
        }
        return sb.toString();
    }
}