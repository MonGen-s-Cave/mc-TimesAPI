# 🕒 McTimesAPI

<div align="center">

![McTimesAPI Logo](https://img.shields.io/badge/McTimesAPI-v1.0.0-blue?style=for-the-badge&logo=minecraft)

**A powerful, modern scheduling library for Bukkit/Paper plugins**

[![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Bukkit](https://img.shields.io/badge/Bukkit-1.20+-green?style=flat-square&logo=minecraft)](https://bukkit.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square)](https://github.com/mongenscave/mctimesapi)

---

*Schedule tasks with natural language syntax and precision timing*

</div>

## ✨ Features

### 🎯 **Natural Language Scheduling**
Schedule tasks using intuitive, human-readable syntax:
```java
TimesAPI.schedule("EVERYDAY @ 18:30", () -> {
    // Your daily task here
});
```

### 🔄 **Multiple Schedule Types**
- **Daily**: `EVERYDAY @ 12:00`
- **Weekdays**: `WEEKDAYS @ 09:00`
- **Weekends**: `WEEKENDS @ 10:00`
- **Weekly**: `EVERY MON,WED,FRI @ 14:00`
- **Monthly**: `EVERY 1ST @ 08:00`, `EVERY LAST DAY @ 23:59`
- **Interval**: `EVERY 30 MINUTES`, `EVERY 2 HOURS`
- **One-time**: `ONCE 2025-12-25 @ 12:00`
- **Range**: `BETWEEN 09:00-17:00 EVERY HOUR`

### ⚡ **Performance Optimized**
- Multi-threaded execution with dedicated thread pool
- Non-blocking CompletableFuture-based API
- Memory-efficient concurrent task management
- Automatic cleanup of completed one-time tasks

### 🎛️ **Flexible Execution Modes**
- **Synchronous**: Execute on main server thread
- **Asynchronous**: Execute on separate thread pool
- **Annotation-based**: Use `@Schedule` for automatic registration

### 🔧 **Developer Friendly**
- Clean, intuitive API design
- Comprehensive error handling
- Full annotation support
- Thread-safe operations

## 🚀 Quick Start

### Installation

**Maven:**
```xml
<dependency>
    <groupId>com.mongenscave</groupId>
    <artifactId>mctimesapi</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

**Gradle:**
```groovy
dependencies {
    compileOnly 'com.mongenscave:mctimesapi:1.0.0'
}
```

## 📚 Usage Examples

### Basic Scheduling

```java
import com.mongenscave.mctimesapi.api.TimesAPI;

public class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Schedule a daily restart warning
        TimesAPI.schedule("EVERYDAY @ 04:00", () -> {
            Bukkit.broadcastMessage("§cServer restart in 30 minutes!");
        });
        
        // Schedule weekend events
        TimesAPI.scheduleAsync("WEEKENDS @ 20:00", () -> {
            startWeekendEvent();
        });
        
        // Auto-save every hour
        TimesAPI.schedule("EVERY 1 HOUR", () -> {
            savePlayerData();
        });
    }
}
```

### Annotation-Based Scheduling

```java
public class EventManager {
    
    @Schedule("EVERYDAY @ 12:00")
    public void dailyReward() {
        // Give players daily rewards
        Bukkit.getOnlinePlayers().forEach(player -> {
            giveReward(player);
        });
    }
    
    @Schedule(value = "EVERY 15 MINUTES", async = true)
    public void cleanupTask() {
        // Perform cleanup operations
        performCleanup();
    }
    
    @Schedule("EVERY MON @ 18:00")
    public void weeklyMaintenance() {
        // Weekly server maintenance
        performMaintenance();
    }
}

// Register the class
TimesAPI.registerScheduledClass(new EventManager());
```

### Advanced Scheduling

```java
// Schedule with callback access to task info
TimesAPI.schedule("EVERYDAY @ 03:00", (task) -> {
    getLogger().info("Backup task executed " + 
                    task.getExecutionCount() + " times");
    performBackup();
});

// One-time scheduled event
TimesAPI.schedule("ONCE 2025-12-31 @ 23:59", () -> {
    Bukkit.broadcastMessage("🎉 Happy New Year!");
});

// Range-based scheduling
TimesAPI.schedule("BETWEEN 09:00-17:00 EVERY 2 HOURS", () -> {
    sendBusinessHoursReminder();
});
```

### Task Management

```java
// Schedule and get task reference
CompletableFuture<ScheduleTask> future = TimesAPI.schedule("EVERY 5 MINUTES", () -> {
    checkPlayerActivity();
});

future.thenAccept(task -> {
    String taskId = task.getId();
    
    // Cancel the task later if needed
    TimesAPI.cancelTask(taskId);
});

// Check active tasks
int activeCount = TimesAPI.getActiveTaskCount();
getLogger().info("Currently running " + activeCount + " scheduled tasks");
```

## 📋 Schedule Syntax Reference

### Time Formats
- `HH:MM` - 24-hour format (e.g., `14:30`, `09:15`)

### Daily Patterns
| Pattern | Description | Example |
|---------|-------------|---------|
| `EVERYDAY @ TIME` | Execute every day | `EVERYDAY @ 18:00` |
| `WEEKDAYS @ TIME` | Monday to Friday | `WEEKDAYS @ 09:00` |
| `WEEKENDS @ TIME` | Saturday and Sunday | `WEEKENDS @ 11:00` |

### Weekly Patterns
| Pattern | Description | Example |
|---------|-------------|---------|
| `EVERY DAY @ TIME` | Specific days | `EVERY MON,WED,FRI @ 14:00` |

**Day Abbreviations:** `MON`, `TUE`, `WED`, `THU`, `FRI`, `SAT`, `SUN`

### Monthly Patterns
| Pattern | Description | Example |
|---------|-------------|---------|
| `EVERY 1ST @ TIME` | First day of month | `EVERY 1ST @ 08:00` |
| `EVERY 15TH @ TIME` | 15th day of month | `EVERY 15TH @ 12:00` |
| `EVERY LAST DAY @ TIME` | Last day of month | `EVERY LAST DAY @ 23:59` |
| `EVERY 1ST MON @ TIME` | First Monday | `EVERY 1ST MON @ 10:00` |
| `EVERY LAST FRI @ TIME` | Last Friday | `EVERY LAST FRI @ 17:00` |

### Interval Patterns
| Pattern | Description | Example |
|---------|-------------|---------|
| `EVERY X MINUTES` | Every X minutes | `EVERY 30 MINUTES` |
| `EVERY X HOURS` | Every X hours | `EVERY 2 HOURS` |
| `EVERY X DAYS` | Every X days | `EVERY 3 DAYS` |

### One-Time Patterns
| Pattern | Description | Example |
|---------|-------------|---------|
| `ONCE YYYY-MM-DD @ TIME` | Execute once | `ONCE 2025-12-25 @ 12:00` |

### Range Patterns
| Pattern | Description | Example |
|---------|-------------|---------|
| `BETWEEN START-END EVERY INTERVAL` | Execute within time range | `BETWEEN 09:00-17:00 EVERY HOUR` |

## 🔧 API Reference

### Core Methods

#### Scheduling Tasks
```java
// Synchronous scheduling
CompletableFuture<ScheduleTask> schedule(String scheduleString, Runnable task)
CompletableFuture<ScheduleTask> schedule(String scheduleString, Consumer<ScheduleTask> taskConsumer)

// Asynchronous scheduling
CompletableFuture<ScheduleTask> scheduleAsync(String scheduleString, Runnable task)
CompletableFuture<ScheduleTask> scheduleAsync(String scheduleString, Consumer<ScheduleTask> taskConsumer)
```

#### Task Management
```java
// Cancel a task
boolean cancelTask(String taskId)

// Get active task count
int getActiveTaskCount()

// Register annotation-based class
void registerScheduledClass(Object instance)

// Check if API is initialized
boolean isInitialized()
```

### ScheduleTask Properties
```java
String getId()                    // Unique task identifier
String getScheduleString()        // Original schedule string
ScheduleConfig getConfig()        // Parsed configuration
boolean isAsync()                 // Execution mode
LocalDateTime getCreatedAt()      // Creation timestamp
LocalDateTime getLastExecution()  // Last execution time
LocalDateTime getNextExecution()  // Next scheduled execution
long getExecutionCount()          // Number of executions
boolean isCancelled()             // Cancellation status
```

## 🛠️ Best Practices

### Performance Tips
- Use async scheduling for heavy operations
- Avoid scheduling too many tasks with short intervals
- Cancel unused tasks to free resources
- Use appropriate schedule patterns for your needs

### Error Handling
```java
TimesAPI.schedule("EVERYDAY @ 12:00", () -> {
    try {
        riskyOperation();
    } catch (Exception e) {
        getLogger().warning("Task failed: " + e.getMessage());
    }
});
```

### Resource Management
```java
@Override
public void onDisable() {
    // Tasks are automatically cleaned up when McTimesAPI disables
    // But you can manually cancel specific tasks if needed
    TimesAPI.cancelTask(taskId);
}
```

## 🤝 Contributing

We welcome contributions! Please feel free to submit issues, feature requests, or pull requests.

### Development Setup
1. Clone the repository
2. Import into your IDE
3. Build with Maven: `mvn clean install`
4. Test with a local server

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: [GitHub Wiki](https://github.com/mongenscave/mctimesapi/wiki)
- **Issues**: [GitHub Issues](https://github.com/mongenscave/mctimesapi/issues)
- **Discord**: [Development Server](https://discord.gg/example)

---

<div align="center">

**Made with ❤️ for the Minecraft community**

*McTimesAPI - Because timing matters*

</div>