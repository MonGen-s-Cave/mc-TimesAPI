package com.mongenscave.mctimesapi.models;

import com.mongenscave.mctimesapi.identifiers.ScheduleType;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
public class ScheduleConfig {
    private ScheduleType type;
    private LocalTime time;
    private LocalDate specificDate;
    private Set<DayOfWeek> daysOfWeek;
    private int dayOfMonth;
    private boolean lastDayOfMonth;
    private DayOfWeek firstWeekdayOfMonth;
    private DayOfWeek lastWeekdayOfMonth;
    private Duration intervalDuration;
    private LocalTime startTime;
    private LocalTime endTime;
    private Duration rangeInterval;
}