package com.mongenscave.mctimesapi.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ScheduleTask {
    @Getter private final String id;
    @Getter private final String scheduleString;
    @Getter private final ScheduleConfig config;
    private final Runnable task;
    @Getter private final boolean async;
    @Getter private final LocalDateTime createdAt;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicLong executionCount = new AtomicLong(0);
    @Getter private volatile LocalDateTime lastExecution;
    @Setter @Getter private volatile LocalDateTime nextExecution;
    private CompletableFuture<Void> taskFuture;

    public ScheduleTask(String scheduleString, ScheduleConfig config, Runnable task, boolean async) {
        this.id = UUID.randomUUID().toString();
        this.scheduleString = scheduleString;
        this.config = config;
        this.task = task;
        this.async = async;
        this.createdAt = LocalDateTime.now();
    }

    public void execute() {
        if (cancelled.get()) return;

        lastExecution = LocalDateTime.now();
        executionCount.incrementAndGet();

        try {
            task.run();
        } catch (Exception exception) {
            System.err.println("Error executing scheduled task '" + scheduleString + "': " + exception.getMessage());
        }
    }

    public boolean cancel() {
        boolean wasCancelled = cancelled.compareAndSet(false, true);
        if (wasCancelled && taskFuture != null) taskFuture.cancel(true);
        return wasCancelled;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public long getExecutionCount() {
        return executionCount.get();
    }

    /**
     * Get a human-readable description of this task
     */
    public String getDescription() {
        return String.format("Task[%s] - Schedule: %s, Async: %s, Executions: %d",
                id.substring(0, 8), scheduleString, async, getExecutionCount());
    }

    /**
     * Check if this task is due for execution
     */
    public boolean isDue() {
        return nextExecution != null &&
                !nextExecution.isAfter(LocalDateTime.now()) &&
                !cancelled.get();
    }

    @Override
    public String toString() {
        return getDescription();
    }
}