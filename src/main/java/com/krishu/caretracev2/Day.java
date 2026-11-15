package com.krishu.caretracev2;

import java.time.DayOfWeek;

public enum Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public static Day from(DayOfWeek day){
        return Day.valueOf(day.name());
    }
}
