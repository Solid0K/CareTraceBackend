package com.krishu.caretracev2.DTO;

import com.krishu.caretracev2.AdministrationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MedicationAdministrationResponse {
    private String id;
    private String medicationId;
    private String medicationName;
    private String dosage;
    private LocalDateTime scheduledAt;
    private AdministrationStatus status;
    private LocalDateTime takenAt;
    private String note;
}
