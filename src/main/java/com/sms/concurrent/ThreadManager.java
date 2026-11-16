package com.sms.concurrent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise thread and concurrency manager.
 * Manages CPU/IO worker pools, scheduled tasks (such as background auto-save),
 * and registers JVM shutdown hooks for graceful termination.
 */
public class ThreadManager {
    private static final Logger LOGGER = Logger.getLogger(ThreadManager.class.getName());
    private static final ThreadManager INSTANCE = new ThreadManager();

    private final ExecutorService workerExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final AtomicInteger threadCounter = new AtomicInteger(1);
    private volatile boolean isShutdown = false;

    private ThreadManager() {
        int corePool = Math.max(4, Runtime.getRuntime().availableProcessors());
        
        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r, "sms-worker-" + threadCounter.getAndIncrement());
            t.setDaemon(true);
            return t;
        };

        this.workerExecutor = new ThreadPoolExecutor(
                corePool,
                corePool * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sms-scheduler-daemon");
            t.setDaemon(true);
            return t;
        });

        // Register graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "sms-shutdown-hook"));
    }

    public static ThreadManager getInstance() {
        return INSTANCE;
    }

    public ExecutorService getWorkerExecutor() {
        return workerExecutor;
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    /**
     * Executes a runnable asynchronously on the worker thread pool.
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, workerExecutor);
    }

    /**
     * Executes a supplier asynchronously on the worker thread pool.
     */
    public <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, workerExecutor);
    }

    /**
     * Schedules a recurring task.
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /**
     * Gracefully shuts down all executors.
     */
    public synchronized void shutdown() {
        if (isShutdown) return;
        isShutdown = true;
        LOGGER.info("Initiating graceful shutdown of SMS ThreadManager...");

        scheduledExecutor.shutdown();
        workerExecutor.shutdown();

        try {
            if (!workerExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                workerExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
            LOGGER.info("SMS ThreadManager terminated cleanly.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            workerExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
        }
    }
}
