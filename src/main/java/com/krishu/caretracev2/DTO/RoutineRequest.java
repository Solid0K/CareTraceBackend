package com.krishu.caretracev2.DTO;

import com.krishu.caretracev2.ActivityType;
import com.krishu.caretracev2.Day;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class RoutineRequest {
    private String title;
    private String description;
    private LocalTime time;
    private List<Day> days;
    private ActivityType activityType;
}
