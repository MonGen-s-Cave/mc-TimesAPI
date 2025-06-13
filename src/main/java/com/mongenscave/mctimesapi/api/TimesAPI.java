package com.mongenscave.mctimesapi.api;

import com.mongenscave.mctimesapi.McTimesAPI;
import com.mongenscave.mctimesapi.manager.SchedulerManager;
import com.mongenscave.mctimesapi.models.ScheduleTask;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * TimesAPI - Main API interface for scheduling tasks
 * This is the primary interface that other plugins should use
 */
public class TimesAPI {
    private static SchedulerManager schedulerManager;

    /**
     * Initialize the API with a scheduler manager
     * This is called internally by McTimesAPI
     *
     * @param manager The scheduler manager instance
     */
    public static void initialize(SchedulerManager manager) {
        schedulerManager = manager;
    }

    /**
     * Schedule a synchronous task with the given schedule string
     *
     * @param scheduleString The schedule instruction (e.g., "EVERYDAY @ 18:00")
     * @param task The task to execute
     * @return CompletableFuture for the scheduled task
     * @throws IllegalStateException if the API is not initialized
     */
    public static CompletableFuture<ScheduleTask> schedule(String scheduleString, Runnable task) {
        validateInitialization();
        return schedulerManager.scheduleTask(scheduleString, task, false);
    }

    /**
     * Schedule an asynchronous task with the given schedule string
     *
     * @param scheduleString The schedule instruction
     * @param task The task to execute
     * @return CompletableFuture for the scheduled task
     * @throws IllegalStateException if the API is not initialized
     */
    public static CompletableFuture<ScheduleTask> scheduleAsync(String scheduleString, Runnable task) {
        validateInitialization();
        return schedulerManager.scheduleTask(scheduleString, task, true);
    }

    /**
     * Schedule a task with callback that receives the ScheduleTask instance
     *
     * @param scheduleString The schedule instruction
     * @param taskConsumer Consumer that receives the ScheduleTask
     * @return CompletableFuture for the scheduled task
     * @throws IllegalStateException if the API is not initialized
     */
    public static CompletableFuture<ScheduleTask> schedule(String scheduleString, Consumer<ScheduleTask> taskConsumer) {
        validateInitialization();
        return schedulerManager.scheduleTaskWithCallback(scheduleString, taskConsumer, false);
    }

    /**
     * Schedule an async task with callback that receives the ScheduleTask instance
     *
     * @param scheduleString The schedule instruction
     * @param taskConsumer Consumer that receives the ScheduleTask
     * @return CompletableFuture for the scheduled task
     * @throws IllegalStateException if the API is not initialized
     */
    public static CompletableFuture<ScheduleTask> scheduleAsync(String scheduleString, Consumer<ScheduleTask> taskConsumer) {
        validateInitialization();
        return schedulerManager.scheduleTaskWithCallback(scheduleString, taskConsumer, true);
    }

    /**
     * Cancel a scheduled task by its ID
     *
     * @param taskId The task ID to cancel
     * @return true if the task was successfully cancelled
     * @throws IllegalStateException if the API is not initialized
     */
    public static boolean cancelTask(String taskId) {
        validateInitialization();
        return schedulerManager.cancelTask(taskId);
    }

    /**
     * Get the number of currently active tasks
     *
     * @return The count of active tasks
     * @throws IllegalStateException if the API is not initialized
     */
    public static int getActiveTaskCount() {
        validateInitialization();
        return schedulerManager.getActiveTaskCount();
    }

    /**
     * Register a class instance with @Schedule annotated methods
     * This is a convenience method that delegates to McTimesAPI
     *
     * @param instance The instance containing @Schedule annotated methods
     * @throws IllegalStateException if the API is not initialized
     */
    public static void registerScheduledClass(Object instance) {
        validateInitialization();
        McTimesAPI.getInstance().registerScheduledClass(instance);
    }

    /**
     * Check if the TimesAPI is properly initialized
     *
     * @return true if the API is ready to use
     */
    public static boolean isInitialized() {
        return schedulerManager != null && McTimesAPI.getInstance() != null;
    }

    /**
     * Get the McTimesAPI instance
     *
     * @return The McTimesAPI instance
     * @throws IllegalStateException if the API is not initialized
     */
    public static McTimesAPI getLibraryInstance() {
        if (McTimesAPI.getInstance() == null) {
            throw new IllegalStateException("McTimesAPI is not loaded! Make sure the McTimesAPI plugin is installed and enabled.");
        }
        return McTimesAPI.getInstance();
    }

    /**
     * Validate that the API is properly initialized
     *
     * @throws IllegalStateException if the API is not initialized
     */
    private static void validateInitialization() {
        if (schedulerManager == null) {
            throw new IllegalStateException("TimesAPI is not initialized! Make sure the McTimesAPI plugin is loaded and enabled.");
        }
    }
}