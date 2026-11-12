package com.krishu.caretracev2.Repository;

import com.krishu.caretracev2.Model.Medication;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface MedicationRepo extends MongoRepository<Medication,String> {
    List<Medication> findByPatientId(String patientId);
    List<Medication> findByRefillDueDateLessThanEqual(LocalDate date);
}
