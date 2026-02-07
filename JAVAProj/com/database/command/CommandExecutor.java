package com.database.command;

import com.database.core.InMemoryDatabase;
import com.database.exception.DatabaseStoppedException;
import com.database.exception.KeyNotFoundException;

public class CommandExecutor {
    private final InMemoryDatabase<String> database;

    public CommandExecutor(InMemoryDatabase<String> database) {
        this.database = database;
    }

    public String execute(Command command) {
        try {
            switch (command.getType()) {
                case PUT:
                    return executePut(command);
                case GET:
                    return executeGet(command);
                case DELETE:
                    return executeDelete(command);
                case STOP:
                    return executeStop();
                case START:
                    return executeStart();
                case EXIT:
                    return "EXIT";
                default:
                    return "Unknown command";
            }
        } catch (DatabaseStoppedException e) {
            return "Error: " + e.getMessage();
        } catch (KeyNotFoundException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String executePut(Command command) {
        if (command.getTtl() != null) {
            database.put(command.getKey(), command.getRawValue(), command.getTtl());
            return "OK (with TTL: " + command.getTtl() + "ms)";
        } else {
            database.put(command.getKey(), command.getRawValue());
            return "OK";
        }
    }

    private String executeGet(Command command) {
        String value = database.get(command.getKey());
        if (value == null) {
            return "NULL";
        }
        return value;
    }

    private String executeDelete(Command command) {
        database.delete(command.getKey());
        return "OK";
    }

    private String executeStop() {
        database.stop();
        return "Database stopped";
    }

    private String executeStart() {
        database.start();
        return "Database started";
    }
}