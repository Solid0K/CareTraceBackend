package com.krishu.caretracev2.Repository;

import com.krishu.caretracev2.Model.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReminderRepo extends MongoRepository<Reminder,String> {
    List<Reminder> findByPatientId(String patientId);
    void deleteByMedicationId(String medicationId);
    void deleteByRoutineId(String routineId);
}
