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

        if (async) taskFuture = CompletableFuture.runAsync(task);
        else {
            try {
                task.run();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public boolean cancel() {
        boolean wasCancelled = cancelled.compareAndSet(false, true);
        if (wasCancelled && taskFuture != null) taskFuture.cancel(true);
        return wasCancelled;
    }

    public boolean isCancelled() { return cancelled.get(); }
    public long getExecutionCount() { return executionCount.get(); }
}
