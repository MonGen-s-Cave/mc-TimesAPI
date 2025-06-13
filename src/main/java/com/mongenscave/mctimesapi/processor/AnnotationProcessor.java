package com.mongenscave.mctimesapi.processor;

import com.mongenscave.mctimesapi.annotations.Schedule;
import com.mongenscave.mctimesapi.manager.SchedulerManager;
import com.mongenscave.mctimesapi.models.ScheduleTask;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class AnnotationProcessor {
    private final SchedulerManager schedulerManager;

    public AnnotationProcessor(SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    public void processScheduledMethods(@NotNull Object instance) {
        Class<?> clazz = instance.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            Schedule scheduleAnnotation = method.getAnnotation(Schedule.class);

            if (scheduleAnnotation != null) {
                method.setAccessible(true);

                String scheduleString = scheduleAnnotation.value();
                boolean async = scheduleAnnotation.async();

                Runnable task = () -> {
                    try {
                        method.invoke(instance);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                };

                CompletableFuture<ScheduleTask> future = schedulerManager.scheduleTask(scheduleString, task, async);

                future.thenAccept(scheduleTask -> {});
            }
        }
    }
}
