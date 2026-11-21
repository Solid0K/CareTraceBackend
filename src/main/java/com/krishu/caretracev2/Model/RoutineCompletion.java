package com.krishu.caretracev2.Model;

import com.krishu.caretracev2.ActivityType;
import com.krishu.caretracev2.ClientRole;
import com.krishu.caretracev2.CompletionStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection="routine_completion")
public class RoutineCompletion {
    @Id
    private String id;
    private String routineId;
    private String patientId;
    private String title;
    private ActivityType activityType;
    private LocalDateTime scheduledAt;
    private CompletionStatus completionStatus;
    private LocalDateTime completedAt;
    private ClientRole completedByRole;
    private String completedByUserId;
    private Boolean careTakerConfirmed=false;
    private LocalDateTime confirmAt;
    private String note;
}
