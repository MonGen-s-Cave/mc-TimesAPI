package com.mongenscave.mctimesapi;

import com.mongenscave.mctimesapi.manager.SchedulerManager;
import com.mongenscave.mctimesapi.models.ScheduleTask;
import com.mongenscave.mctimesapi.processor.AnnotationProcessor;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * TimesAPI - Standalone scheduling library for any Java application
 * Usage:
 * 1. Create an instance: TimesAPI api = new TimesAPI();
 * 2. Schedule tasks: api.schedule("EVERYDAY @ 18:00", () -> { ... });
 * 3. Use annotations: api.registerScheduledClass(yourClassInstance);
 * 4. Shutdown when done: api.shutdown();
 */
public class TimesAPI {
    private final SchedulerManager schedulerManager;
    private final AnnotationProcessor annotationProcessor;
    @Getter private boolean initialized;

    /**
     * Create a new TimesAPI instance
     */
    public TimesAPI() {
        this.schedulerManager = new SchedulerManager();
        this.annotationProcessor = new AnnotationProcessor(schedulerManager);
        this.initialized = true;
    }

    /**
     * Schedule a synchronous task with the given schedule string
     *
     * @param scheduleString The schedule instruction (e.g., "EVERYDAY @ 18:00")
     * @param task The task to execute
     * @return CompletableFuture for the scheduled task
     */
    public CompletableFuture<ScheduleTask> schedule(String scheduleString, Runnable task) {
        validateInitialization();
        return schedulerManager.scheduleTask(scheduleString, task, false);
    }

    /**
     * Schedule an asynchronous task with the given schedule string
     *
     * @param scheduleString The schedule instruction
     * @param task The task to execute
     * @return CompletableFuture for the scheduled task
     */
    public CompletableFuture<ScheduleTask> scheduleAsync(String scheduleString, Runnable task) {
        validateInitialization();
        return schedulerManager.scheduleTask(scheduleString, task, true);
    }

    /**
     * Schedule a task with callback that receives the ScheduleTask instance
     *
     * @param scheduleString The schedule instruction
     * @param taskConsumer Consumer that receives the ScheduleTask
     * @return CompletableFuture for the scheduled task
     */
    public CompletableFuture<ScheduleTask> schedule(String scheduleString, Consumer<ScheduleTask> taskConsumer) {
        validateInitialization();
        return schedulerManager.scheduleTaskWithCallback(scheduleString, taskConsumer, false);
    }

    /**
     * Schedule an async task with callback that receives the ScheduleTask instance
     *
     * @param scheduleString The schedule instruction
     * @param taskConsumer Consumer that receives the ScheduleTask
     * @return CompletableFuture for the scheduled task
     */
    public CompletableFuture<ScheduleTask> scheduleAsync(String scheduleString, Consumer<ScheduleTask> taskConsumer) {
        validateInitialization();
        return schedulerManager.scheduleTaskWithCallback(scheduleString, taskConsumer, true);
    }

    /**
     * Cancel a scheduled task by its ID
     *
     * @param taskId The task ID to cancel
     * @return true if the task was successfully cancelled
     */
    public boolean cancelTask(String taskId) {
        validateInitialization();
        return schedulerManager.cancelTask(taskId);
    }

    /**
     * Get the number of currently active tasks
     *
     * @return The count of active tasks
     */
    public int getActiveTaskCount() {
        validateInitialization();
        return schedulerManager.getActiveTaskCount();
    }

    /**
     * Register a class instance with @Schedule annotated methods
     *
     * @param instance The instance containing @Schedule annotated methods
     */
    public void registerScheduledClass(Object instance) {
        validateInitialization();
        annotationProcessor.processScheduledMethods(instance);
    }

    /**
     * Shutdown the TimesAPI and clean up all resources
     * Call this when your application is shutting down
     */
    public void shutdown() {
        schedulerManager.shutdown();
        initialized = false;
    }

    /**
     * Validate that the API is properly initialized
     */
    private void validateInitialization() {
        if (!initialized) throw new IllegalStateException("TimesAPI is not properly initialized!");
    }
}