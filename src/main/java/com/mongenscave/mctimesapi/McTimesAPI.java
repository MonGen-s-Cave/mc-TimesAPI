package com.mongenscave.mctimesapi;

import com.mongenscave.mctimesapi.api.TimesAPI;
import com.mongenscave.mctimesapi.manager.SchedulerManager;
import com.mongenscave.mctimesapi.processor.AnnotationProcessor;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * McTimesAPI - A powerful scheduling library for Bukkit/Paper plugins
 * This class serves as the main entry point for the library
 */
@SuppressWarnings("deprecation")
public final class McTimesAPI extends JavaPlugin {

    @Getter private static McTimesAPI instance;
    @Getter private SchedulerManager schedulerManager;
    @Getter private AnnotationProcessor annotationProcessor;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        schedulerManager = new SchedulerManager(this);
        annotationProcessor = new AnnotationProcessor(schedulerManager);

        TimesAPI.initialize(schedulerManager);
    }

    @Override
    public void onDisable() {
        if (schedulerManager != null) schedulerManager.shutdown();
    }

    /**
     * Register a class with @Schedule annotated methods
     * This method should be called by other plugins to register their scheduled methods
     *
     * @param instance The instance of the class containing @Schedule annotated methods
     */
    public void registerScheduledClass(Object instance) {
        if (annotationProcessor != null) annotationProcessor.processScheduledMethods(instance);
        else getLogger().warning("Cannot register scheduled class - AnnotationProcessor is not initialized!");
    }

    /**
     * Check if the library is properly initialized
     *
     * @return true if the library is ready to use
     */
    public boolean isInitialized() {
        return schedulerManager != null && annotationProcessor != null;
    }

    /**
     * Get library version info
     *
     * @return Version string
     */
    public @NotNull String getLibraryVersion() {
        return getDescription().getVersion();
    }
}