package com.krishu.caretracev2.Repository;

import com.krishu.caretracev2.Model.LocationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LocationHistoryRepo extends MongoRepository<LocationHistory,String> {
    Optional<LocationHistory> findFirstByPatientIdOrderByRecordedAtDesc(String patientId);
    List<LocationHistory> findByPatientIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            String patientId, LocalDateTime from, LocalDateTime to);
    long deleteByRecordedAtBefore(LocalDateTime time);
}
