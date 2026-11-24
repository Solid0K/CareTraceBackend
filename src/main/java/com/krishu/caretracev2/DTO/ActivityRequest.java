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
    private String note;
}
