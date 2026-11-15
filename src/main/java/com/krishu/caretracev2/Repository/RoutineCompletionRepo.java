package com.krishu.caretracev2.Repository;

import com.krishu.caretracev2.CompletionStatus;
import com.krishu.caretracev2.Model.RoutineCompletion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface RoutineCompletionRepo extends MongoRepository<RoutineCompletion,String> {
    boolean existsByRoutineIdAndScheduledAtBetween(String routineId, LocalDateTime from, LocalDateTime to);
    void deleteByRoutineIdAndCompletionStatus(String routineId, CompletionStatus status);
}
