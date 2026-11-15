package com.krishu.caretracev2.Model;

import com.krishu.caretracev2.ActivityType;
import com.krishu.caretracev2.Day;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Document(collection="routine")
public class Routine {
    @Id
    private String id;
    private String patientId;
    private String title;
    private String description;
    private LocalTime time;
    private List<Day> days=new ArrayList<>();
    private ActivityType activityType;
}
