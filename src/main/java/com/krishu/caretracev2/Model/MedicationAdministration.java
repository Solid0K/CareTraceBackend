package com.krishu.caretracev2.Model;

import com.krishu.caretracev2.AdministrationStatus;
import com.krishu.caretracev2.ClientRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection="medication_administration")
public class MedicationAdministration {
    @Id
    private String id;
    private String patientId;
    private String medicationId;
    private String reminderId;
    private String medicationName;
    private String dosage;
    private LocalDateTime scheduledAt;
    private AdministrationStatus status;
    private LocalDateTime takenAt;
    private String administrationUserId;
    private ClientRole administrationByRole;
    private String note;
}
