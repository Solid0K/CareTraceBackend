package com.krishu.caretracev2.DTO;

import com.krishu.caretracev2.ActivityType;
import com.krishu.caretracev2.ClientRole;
import com.krishu.caretracev2.CompletionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ActivityRequest {
    private String completionId;
    private String routineId;
    private String title;
    private ActivityType activityType;
    private LocalDateTime scheduledAt;
    private CompletionStatus status;
    private LocalDateTime completedAt;
    private ClientRole completedByRole;
    private Boolean careTakerConfirmed;
    private LocalDateTime confirmedAt;
    private String note;
}
