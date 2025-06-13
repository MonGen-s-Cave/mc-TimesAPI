package com.mongenscave.mctimesapi.math;

import com.mongenscave.mctimesapi.models.ScheduleConfig;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public class TaskCalculator {
    public LocalDateTime calculateNextExecution(@NotNull ScheduleConfig config) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime targetTime = config.getTime() != null ? config.getTime() : LocalTime.now();

        return switch (config.getType()) {
            case DAILY -> calculateDaily(now, targetTime);
            case WEEKDAYS -> calculateWeekdays(now, targetTime);
            case WEEKENDS -> calculateWeekends(now, targetTime);
            case WEEKLY -> calculateWeekly(now, config);
            case MONTHLY -> calculateMonthly(now, config);
            case INTERVAL -> calculateInterval(now, config);
            case ONCE -> calculateOnce(config);
            case RANGE -> calculateRange(now, config);
        };
    }

    private @NotNull LocalDateTime calculateDaily(@NotNull LocalDateTime now, LocalTime targetTime) {
        LocalDateTime target = now.toLocalDate().atTime(targetTime);
        if (target.isBefore(now) || target.equals(now)) target = target.plusDays(1);
        return target;
    }

    private LocalDateTime calculateWeekdays(@NotNull LocalDateTime now, LocalTime targetTime) {
        LocalDateTime target = now.toLocalDate().atTime(targetTime);

        while (target.isBefore(now) || target.equals(now) ||
                target.getDayOfWeek() == DayOfWeek.SATURDAY ||
                target.getDayOfWeek() == DayOfWeek.SUNDAY) {
            target = target.plusDays(1);
        }

        return target;
    }

    private LocalDateTime calculateWeekends(@NotNull LocalDateTime now, LocalTime targetTime) {
        LocalDateTime target = now.toLocalDate().atTime(targetTime);

        while (target.isBefore(now)
                || target.equals(now)
                || (target.getDayOfWeek() != DayOfWeek.SATURDAY && target.getDayOfWeek() != DayOfWeek.SUNDAY)) {
            target = target.plusDays(1);
        }

        return target;
    }

    private LocalDateTime calculateWeekly(LocalDateTime now, @NotNull ScheduleConfig config) {
        if (config.getDaysOfWeek() == null || config.getDaysOfWeek().isEmpty()) return now.plusDays(1);

        LocalTime targetTime = config.getTime() != null ? config.getTime() : LocalTime.now();
        LocalDateTime target = now.toLocalDate().atTime(targetTime);

        while (target.isBefore(now) || target.equals(now) ||
                !config.getDaysOfWeek().contains(target.getDayOfWeek())) {
            target = target.plusDays(1);
        }

        return target;
    }

    private @NotNull LocalDateTime calculateMonthly(@NotNull LocalDateTime now, @NotNull ScheduleConfig config) {
        LocalTime targetTime = config.getTime() != null ? config.getTime() : LocalTime.now();
        LocalDate targetDate = now.toLocalDate();

        if (config.isLastDayOfMonth()) targetDate = targetDate.with(TemporalAdjusters.lastDayOfMonth());
        else if (config.getDayOfMonth() > 0) targetDate = targetDate.withDayOfMonth(Math.min(config.getDayOfMonth(), targetDate.lengthOfMonth()));
        else if (config.getFirstWeekdayOfMonth() != null) targetDate = targetDate.with(TemporalAdjusters.firstInMonth(config.getFirstWeekdayOfMonth()));
        else if (config.getLastWeekdayOfMonth() != null) targetDate = targetDate.with(TemporalAdjusters.lastInMonth(config.getLastWeekdayOfMonth()));

        LocalDateTime target = targetDate.atTime(targetTime);

        if (target.isBefore(now) || target.equals(now)) target = target.plusMonths(1);
        return target;
    }

    private LocalDateTime calculateInterval(LocalDateTime now, @NotNull ScheduleConfig config) {
        if (config.getIntervalDuration() != null) return now.plus(config.getIntervalDuration());
        return now.plusHours(1);
    }

    private LocalDateTime calculateOnce(@NotNull ScheduleConfig config) {
        if (config.getSpecificDate() != null) {
            LocalTime time = config.getTime() != null ? config.getTime() : LocalTime.now();
            return config.getSpecificDate().atTime(time);
        }

        return LocalDateTime.now().plusMinutes(1);
    }

    private LocalDateTime calculateRange(LocalDateTime now, @NotNull ScheduleConfig config) {
        if (config.getStartTime() != null && config.getEndTime() != null) {
            LocalTime startTime = config.getStartTime();
            LocalTime endTime = config.getEndTime();
            LocalTime nowTime = now.toLocalTime();

            if (nowTime.isBefore(startTime)) return now.toLocalDate().atTime(startTime);
            else if (nowTime.isAfter(endTime)) return now.toLocalDate().plusDays(1).atTime(startTime);
            else if (config.getRangeInterval() != null) return now.plus(config.getRangeInterval());
        }
        return now.plusHours(1);
    }
}
