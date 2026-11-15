package com.krishu.caretracev2.Scheduler;

import com.krishu.caretracev2.CompletionStatus;
import com.krishu.caretracev2.Day;
import com.krishu.caretracev2.Model.Routine;
import com.krishu.caretracev2.Model.RoutineCompletion;
import com.krishu.caretracev2.Repository.RoutineCompletionRepo;
import com.krishu.caretracev2.Repository.RoutineRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class RoutineOccurrenceScheduler {
    private static final Logger log = LoggerFactory.getLogger(RoutineOccurrenceScheduler.class);

    private final RoutineRepo routineRepo;
    private final RoutineCompletionRepo routineCompletionRepo;

    public RoutineOccurrenceScheduler(RoutineRepo routineRepo,RoutineCompletionRepo routineCompletionRepo){
        this.routineRepo=routineRepo;
        this.routineCompletionRepo=routineCompletionRepo;
    }

    @Scheduled(fixedRate=60000)
    public void createRoutineCompletionObject(){
        LocalDateTime now=LocalDateTime.now();
        LocalDate today=now.toLocalDate();
        LocalTime currTime=now.toLocalTime();

        Day todayDay= Day.from(today.getDayOfWeek());
        LocalDateTime start=today.atStartOfDay().minusSeconds(1);
        LocalDateTime end=today.plusDays(1).atStartOfDay();

        for(Routine routine:routineRepo.findAll()){
            try{
                if(routine.getTime()==null || routine.getDays()==null || !routine.getDays().contains(todayDay)
                        || currTime.isBefore(routine.getTime())){
                    continue;
                }

                if(routineCompletionRepo.existsByRoutineIdAndScheduledAtBetween(routine.getId(),start,end)){
                    continue;
                }

                RoutineCompletion routineCompletion=new RoutineCompletion();
                routineCompletion.setRoutineId(routine.getId());
                routineCompletion.setPatientId(routine.getPatientId());
                routineCompletion.setTitle(routine.getTitle());
                routineCompletion.setActivityType(routine.getActivityType());
                routineCompletion.setScheduledAt(today.atTime(routine.getTime()));
                routineCompletion.setCompletionStatus(CompletionStatus.PENDING);
                routineCompletionRepo.save(routineCompletion);
            }catch(RuntimeException e){
                log.warn("Could not create occurrence for routine {}: {}", routine.getId(), e.getMessage());
            }
        }
    }
}
