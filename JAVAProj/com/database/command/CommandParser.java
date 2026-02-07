package com.database.command;

import com.database.exception.InvalidCommandException;
import com.database.exception.InvalidTTLException;

public class CommandParser {

    public Command parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidCommandException("Command cannot be empty");
        }

        String[] tokens = input.trim().split("\\s+");
        String commandName = tokens[0].toUpperCase();

        try {
            CommandType type = CommandType.valueOf(commandName);

            switch (type) {
                case PUT:
                    return parsePut(tokens);
                case GET:
                    return parseGet(tokens);
                case DELETE:
                    return parseDelete(tokens);
                case STOP:
                case START:
                case EXIT:
                    return new Command(type);
                default:
                    throw new InvalidCommandException("Unknown command: " + commandName);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException("Invalid command: " + commandName);
        }
    }

    private Command parsePut(String[] tokens) {
        if (tokens.length < 3) {
            throw new InvalidCommandException("PUT requires at least key and value");
        }

        Integer key = parseKey(tokens[1]);
        String value = tokens[2];

        if (tokens.length == 3) {
            return new Command(CommandType.PUT, key, value);
        } else if (tokens.length == 4) {
            Long ttl = parseTTL(tokens[3]);
            return new Command(CommandType.PUT, key, value, ttl);
        } else {
            throw new InvalidCommandException("PUT accepts only key, value, and optional TTL");
        }
    }

    private Command parseGet(String[] tokens) {
        if (tokens.length != 2) {
            throw new InvalidCommandException("GET requires exactly one key");
        }

        Integer key = parseKey(tokens[1]);
        return new Command(CommandType.GET, key);
    }

    private Command parseDelete(String[] tokens) {
        if (tokens.length != 2) {
            throw new InvalidCommandException("DELETE requires exactly one key");
        }

        Integer key = parseKey(tokens[1]);
        return new Command(CommandType.DELETE, key);
    }

    private Integer parseKey(String keyStr) {
        try {
            return Integer.parseInt(keyStr);
        } catch (NumberFormatException e) {
            throw new InvalidCommandException("Key must be an integer: " + keyStr);
        }
    }

    private Long parseTTL(String ttlStr) {
        try {
            long ttl = Long.parseLong(ttlStr);
            if (ttl <= 0) {
                throw new InvalidTTLException("TTL must be positive: " + ttl);
            }
            return ttl;
        } catch (NumberFormatException e) {
            throw new InvalidTTLException("TTL must be a valid number: " + ttlStr);
        }
    }
}