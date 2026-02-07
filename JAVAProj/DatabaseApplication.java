package com.database;

import com.database.cleanup.CleanupTask;
import com.database.command.Command;
import com.database.command.CommandExecutor;
import com.database.command.CommandParser;
import com.database.core.InMemoryDatabase;
import com.database.exception.InvalidCommandException;
import com.database.exception.InvalidTTLException;

import java.util.Scanner;

public class DatabaseApplication {
    private final InMemoryDatabase<String> database;
    private final CommandParser parser;
    private final CommandExecutor executor;
    private final Thread cleanupThread;
    private final CleanupTask cleanupTask;

    public DatabaseApplication() {
        this.database = new InMemoryDatabase<>();
        this.parser = new CommandParser();
        this.executor = new CommandExecutor(database);
        this.cleanupTask = new CleanupTask(database, 1000);
        this.cleanupThread = new Thread(cleanupTask, "Cleanup-Thread");
        this.cleanupThread.setDaemon(true);
    }

    public void start() {
        cleanupThread.start();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================");
        System.out.println("  In-Memory Database with TTL Support");
        System.out.println("=================================================");
        System.out.println("Commands: PUT, GET, DELETE, STOP, START, EXIT");
        System.out.println("=================================================\n");

        while (true) {
            try {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine();
                if (input.trim().isEmpty()) {
                    continue;
                }

                Command command = parser.parse(input);
                String result = executor.execute(command);

                if ("EXIT".equals(result)) {
                    System.out.println("Shutting down database...");
                    break;
                }

                System.out.println(result);

            } catch (InvalidCommandException | InvalidTTLException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }

        cleanupTask.shutdown();
        scanner.close();
        System.out.println("Database shutdown complete.");
    }

    public static void main(String[] args) {
        DatabaseApplication app = new DatabaseApplication();
        app.start();
    }
}