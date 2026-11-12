package com.krishu.caretracev2.Repository;

import com.krishu.caretracev2.AdministrationStatus;
import com.krishu.caretracev2.Model.MedicationAdministration;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicationAdministrationRepo extends MongoRepository<MedicationAdministration,String> {
    boolean existsByReminderIdAndScheduledAt(String id, LocalDateTime scheduledAt);
    List<MedicationAdministration> findByPatientIdAndScheduledAtBetweenOrderByScheduledAtDesc(
            String patientId, LocalDateTime from, LocalDateTime to);

    List<MedicationAdministration> findByStatusAndScheduledAtBefore(AdministrationStatus status, LocalDateTime time);
    void deleteByMedicationIdAndStatus(String medicationId,AdministrationStatus status);
}
