package com.mongenscave.mctimesapi.manager;

import com.mongenscave.mctimesapi.identifiers.ScheduleType;
import com.mongenscave.mctimesapi.math.TaskCalculator;
import com.mongenscave.mctimesapi.models.ScheduleConfig;
import com.mongenscave.mctimesapi.models.ScheduleTask;
import com.mongenscave.mctimesapi.utils.ScheduleParser;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SchedulerManager {
    private final ConcurrentHashMap<String, ScheduleTask> activeTasks;
    private final ScheduledExecutorService executorService;
    private final ScheduledExecutorService asyncExecutorService;
    private final TaskCalculator taskCalculator;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public SchedulerManager() {
        this.activeTasks = new ConcurrentHashMap<>();
        this.executorService = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "TimesAPI-Scheduler-Main");
            t.setDaemon(true);
            return t;
        });
        this.asyncExecutorService = Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r, "TimesAPI-Async-Worker");
            t.setDaemon(true);
            return t;
        });
        this.taskCalculator = new TaskCalculator();

        startSchedulerLoop();
    }

    public CompletableFuture<ScheduleTask> scheduleTask(String scheduleString, Runnable task, boolean async) {
        return CompletableFuture.supplyAsync(() -> {
            ScheduleConfig config = ScheduleParser.parse(scheduleString);
            ScheduleTask scheduleTask = new ScheduleTask(scheduleString, config, task, async);

            LocalDateTime nextExecution = taskCalculator.calculateNextExecution(config);
            scheduleTask.setNextExecution(nextExecution);

            activeTasks.put(scheduleTask.getId(), scheduleTask);

            return scheduleTask;
        }, executorService);
    }

    public CompletableFuture<ScheduleTask> scheduleTaskWithCallback(String scheduleString, Consumer<ScheduleTask> taskConsumer, boolean async) {
        return scheduleTask(scheduleString, () -> {
            ScheduleTask currentTask = getCurrentTask(scheduleString);
            if (currentTask != null) taskConsumer.accept(currentTask);
        }, async);
    }

    private ScheduleTask getCurrentTask(String scheduleString) {
        return activeTasks.values().stream()
                .filter(task -> task.getScheduleString().equals(scheduleString))
                .findFirst()
                .orElse(null);
    }

    public boolean cancelTask(String taskId) {
        ScheduleTask task = activeTasks.remove(taskId);

        if (task != null) return task.cancel();
        return false;
    }

    public int getActiveTaskCount() {
        return activeTasks.size();
    }

    private void startSchedulerLoop() {
        executorService.scheduleAtFixedRate(() -> {
            if (!running.get()) return;

            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

            activeTasks.values().parallelStream()
                    .filter(task -> !task.isCancelled())
                    .filter(task -> task.getNextExecution() != null)
                    .filter(task -> !task.getNextExecution().isAfter(now))
                    .forEach(task -> {
                        try {
                            if (task.isAsync()) CompletableFuture.runAsync(task::execute, asyncExecutorService);
                            else task.execute();

                            if (task.getConfig().getType() != ScheduleType.ONCE) {
                                LocalDateTime nextExecution = taskCalculator.calculateNextExecution(task.getConfig());
                                task.setNextExecution(nextExecution);
                            } else activeTasks.remove(task.getId());
                        } catch (Exception exception) {
                            System.err.println("Error executing scheduled task: " + exception.getMessage());
                        }
                    });

        }, 0, 30, TimeUnit.SECONDS);
    }

    public void shutdown() {
        running.set(false);

        activeTasks.values().forEach(ScheduleTask::cancel);
        activeTasks.clear();

        shutdownExecutorService(executorService, "Main Scheduler");
        shutdownExecutorService(asyncExecutorService, "Async Worker");
    }

    private void shutdownExecutorService(@NotNull ScheduledExecutorService service, String name) {
        service.shutdown();
        try {
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown of " + name + " executor service");
                service.shutdownNow();
                if (!service.awaitTermination(5, TimeUnit.SECONDS)) System.err.println("Failed to shutdown " + name + " executor service");
            }
        } catch (InterruptedException exception) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}