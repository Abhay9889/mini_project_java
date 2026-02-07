package com.database.demo;

import com.database.cleanup.CleanupTask;
import com.database.command.Command;
import com.database.command.CommandExecutor;
import com.database.command.CommandParser;
import com.database.core.InMemoryDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultiThreadedDemo {
    private static final int NUM_THREADS = 5;
    private static final int OPERATIONS_PER_THREAD = 20;

    public static void main(String[] args) throws InterruptedException {
        InMemoryDatabase<String> database = new InMemoryDatabase<>();
        CommandParser parser = new CommandParser();
        CommandExecutor executor = new CommandExecutor(database);

        CleanupTask cleanupTask = new CleanupTask(database, 500);
        Thread cleanupThread = new Thread(cleanupTask, "Cleanup-Thread");
        cleanupThread.setDaemon(true);
        cleanupThread.start();

        System.out.println("=================================================");
        System.out.println("  Multi-Threaded Database Demo");
        System.out.println("=================================================");
        System.out.println("Threads: " + NUM_THREADS);
        System.out.println("Operations per thread: " + OPERATIONS_PER_THREAD);
        System.out.println("=================================================\n");

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                Random random = new Random();
                
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    try {
                        int operation = random.nextInt(4);
                        int key = random.nextInt(100);

                        Command command;
                        String result;

                        switch (operation) {
                            case 0:
                                String value = "value_" + threadId + "_" + j;
                                command = parser.parse("PUT " + key + " " + value);
                                result = executor.execute(command);
                                System.out.println("[Thread-" + threadId + "] PUT " + key + " -> " + result);
                                break;

                            case 1:
                                String valueWithTTL = "ttl_" + threadId + "_" + j;
                                long ttl = 2000 + random.nextInt(3000);
                                command = parser.parse("PUT " + key + " " + valueWithTTL + " " + ttl);
                                result = executor.execute(command);
                                System.out.println("[Thread-" + threadId + "] PUT " + key + " (TTL=" + ttl + ") -> " + result);
                                break;

                            case 2:
                                command = parser.parse("GET " + key);
                                result = executor.execute(command);
                                System.out.println("[Thread-" + threadId + "] GET " + key + " -> " + result);
                                break;

                            case 3:
                                try {
                                    command = parser.parse("DELETE " + key);
                                    result = executor.execute(command);
                                    System.out.println("[Thread-" + threadId + "] DELETE " + key + " -> " + result);
                                } catch (Exception e) {
                                    System.out.println("[Thread-" + threadId + "] DELETE " + key + " -> " + e.getMessage());
                                }
                                break;
                        }

                        Thread.sleep(random.nextInt(100));

                    } catch (Exception e) {
                        System.out.println("[Thread-" + threadId + "] Error: " + e.getMessage());
                    }
                }
            }, "Worker-" + i);

            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n=================================================");
        System.out.println("Testing STOP/START commands...");
        System.out.println("=================================================");

        executor.execute(parser.parse("STOP"));
        System.out.println("Database stopped");

        try {
            executor.execute(parser.parse("PUT 999 test"));
        } catch (Exception e) {
            System.out.println("PUT failed (expected): " + e.getMessage());
        }

        executor.execute(parser.parse("START"));
        System.out.println("Database started");

        String result = executor.execute(parser.parse("PUT 999 test"));
        System.out.println("PUT successful: " + result);

        System.out.println("\n=================================================");
        System.out.println("Testing TTL expiration...");
        System.out.println("=================================================");

        executor.execute(parser.parse("PUT 1000 shortlived 2000"));
        System.out.println("PUT key=1000 with TTL=2000ms");

        result = executor.execute(parser.parse("GET 1000"));
        System.out.println("GET 1000 (immediate): " + result);

        Thread.sleep(3000);

        result = executor.execute(parser.parse("GET 1000"));
        System.out.println("GET 1000 (after 3s): " + result);

        System.out.println("\n=================================================");
        System.out.println("Final database size: " + database.size());
        System.out.println("=================================================");

        cleanupTask.shutdown();
        System.out.println("Demo complete!");
    }
}