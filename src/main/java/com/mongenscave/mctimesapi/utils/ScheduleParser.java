package com.mongenscave.mctimesapi.utils;

import com.mongenscave.mctimesapi.identifiers.ScheduleType;
import com.mongenscave.mctimesapi.models.ScheduleConfig;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleParser {
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("EVERY (\\d+) (DAYS?|HOURS?|MINUTES?|MIN)");

    public static @NotNull ScheduleConfig parse(String scheduleString) {
        scheduleString = scheduleString.trim().toUpperCase();

        ScheduleConfig config = new ScheduleConfig();
        Matcher timeMatcher = TIME_PATTERN.matcher(scheduleString);

        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = Integer.parseInt(timeMatcher.group(2));
            config.setTime(LocalTime.of(hour, minute));
        }

        if (scheduleString.startsWith("EVERYDAY")) config.setType(ScheduleType.DAILY);
        else if (scheduleString.startsWith("WEEKDAYS")) config.setType(ScheduleType.WEEKDAYS);
        else if (scheduleString.startsWith("WEEKENDS")) config.setType(ScheduleType.WEEKENDS);
        else if (scheduleString.contains("EVERY") && scheduleString.contains("@")) parseWeeklySchedule(scheduleString, config);
        else if (scheduleString.contains("EVERY") && (scheduleString.contains("ST")
                || scheduleString.contains("ND")
                || scheduleString.contains("TH"))) parseMonthlySchedule(scheduleString, config);
        else if (INTERVAL_PATTERN.matcher(scheduleString).find()) parseIntervalSchedule(scheduleString, config);
        else if (scheduleString.startsWith("ONCE")) parseOnceSchedule(scheduleString, config);
        else if (scheduleString.contains("BETWEEN")) parseRangeSchedule(scheduleString, config);

        return config;
    }

    private static void parseWeeklySchedule(@NotNull String scheduleString, @NotNull ScheduleConfig config) {
        config.setType(ScheduleType.WEEKLY);
        Set<DayOfWeek> days = new HashSet<>();

        if (scheduleString.contains("MON")) days.add(DayOfWeek.MONDAY);
        if (scheduleString.contains("TUE")) days.add(DayOfWeek.TUESDAY);
        if (scheduleString.contains("WED")) days.add(DayOfWeek.WEDNESDAY);
        if (scheduleString.contains("THU")) days.add(DayOfWeek.THURSDAY);
        if (scheduleString.contains("FRI")) days.add(DayOfWeek.FRIDAY);
        if (scheduleString.contains("SAT")) days.add(DayOfWeek.SATURDAY);
        if (scheduleString.contains("SUN")) days.add(DayOfWeek.SUNDAY);

        config.setDaysOfWeek(days);
    }

    private static void parseMonthlySchedule(@NotNull String scheduleString, @NotNull ScheduleConfig config) {
        config.setType(ScheduleType.MONTHLY);

        if (scheduleString.contains("1ST")) config.setDayOfMonth(1);
        else if (scheduleString.contains("15TH")) config.setDayOfMonth(15);
        else if (scheduleString.contains("LAST DAY")) config.setLastDayOfMonth(true);
        else if (scheduleString.contains("1ST MON")) config.setFirstWeekdayOfMonth(DayOfWeek.MONDAY);
        else if (scheduleString.contains("LAST FRI")) config.setLastWeekdayOfMonth(DayOfWeek.FRIDAY);
    }

    private static void parseIntervalSchedule(String scheduleString, ScheduleConfig config) {
        Matcher matcher = INTERVAL_PATTERN.matcher(scheduleString);

        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            config.setType(ScheduleType.INTERVAL);

            if (unit.startsWith("DAY")) config.setIntervalDuration(Duration.ofDays(value));
            else if (unit.startsWith("HOUR")) config.setIntervalDuration(Duration.ofHours(value));
            else if (unit.startsWith("MIN")) config.setIntervalDuration(Duration.ofMinutes(value));
        }
    }

    private static void parseOnceSchedule(String scheduleString, @NotNull ScheduleConfig config) {
        config.setType(ScheduleType.ONCE);

        Matcher dateMatcher = DATE_PATTERN.matcher(scheduleString);

        if (dateMatcher.find()) {
            int year = Integer.parseInt(dateMatcher.group(1));
            int month = Integer.parseInt(dateMatcher.group(2));
            int day = Integer.parseInt(dateMatcher.group(3));
            config.setSpecificDate(LocalDate.of(year, month, day));
        }
    }

    private static void parseRangeSchedule(String scheduleString, @NotNull ScheduleConfig config) {
        config.setType(ScheduleType.RANGE);

        Pattern rangePattern = Pattern.compile("BETWEEN (\\d{1,2}):(\\d{2})-(\\d{1,2}):(\\d{2})");
        Matcher matcher = rangePattern.matcher(scheduleString);

        if (matcher.find()) {
            LocalTime startTime = LocalTime.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2))
            );
            LocalTime endTime = LocalTime.of(
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4))
            );

            config.setStartTime(startTime);
            config.setEndTime(endTime);

            if (scheduleString.contains("EVERY HOUR")) config.setRangeInterval(Duration.ofHours(1));
        }
    }
}