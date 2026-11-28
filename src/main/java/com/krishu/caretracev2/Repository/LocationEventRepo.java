package com.krishu.caretracev2.Repository;

import com.krishu.caretracev2.Model.LocationEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LocationEventRepo extends MongoRepository<LocationEvent,String> {
    List<LocationEvent> findByPatientIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            String patientId, LocalDateTime from, LocalDateTime to);
    long deleteByOccurredAtBefore(LocalDateTime time);
}
