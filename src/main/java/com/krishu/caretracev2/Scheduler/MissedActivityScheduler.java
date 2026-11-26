package com.krishu.caretracev2.Scheduler;

import com.krishu.caretracev2.CompletionStatus;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Model.RoutineCompletion;
import com.krishu.caretracev2.NotificationType;
import com.krishu.caretracev2.Repository.PatientRepo;
import com.krishu.caretracev2.Repository.RoutineCompletionRepo;
import com.krishu.caretracev2.Service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class MissedActivityScheduler {

    private static final Logger log = LoggerFactory.getLogger(MissedActivityScheduler.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final RoutineCompletionRepo routineCompletionRepo;
    private final PatientRepo patientRepo;
    private final NotificationService notificationService;

    @Value("${routine.misssed.grace.minutes}")
    private long graceMinutes;

    public MissedActivityScheduler(RoutineCompletionRepo routineCompletionRepo, PatientRepo patientRepo,
                                   NotificationService notificationService){

        this.routineCompletionRepo = routineCompletionRepo;
        this.patientRepo = patientRepo;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate=60000)
    public void markAsMissed(){
        LocalDateTime cutoff=LocalDateTime.now().minusMinutes(graceMinutes);
        List<RoutineCompletion> overdue=routineCompletionRepo
                .findByCompletionStatusAndScheduledAtBefore(CompletionStatus.PENDING,cutoff);

        for(RoutineCompletion activity:overdue){
            try{
                Patient patient=patientRepo.findById(activity.getPatientId()).orElse(null);
                if(patient!=null){
                    String message="Missed activity: "+activity.getTitle()+" was due at "
                            +activity.getScheduledAt().format(TIME_FORMAT);
                    notificationService.createNotification(patient.getCareTakerId(),patient.getId(),
                            null, NotificationType.ROUTINE_MISSED,message);
                }
                activity.setCompletionStatus(CompletionStatus.MISSED);
                routineCompletionRepo.save(activity);
            }catch(RuntimeException e){
                log.warn("Could not mark activity {} as missed: {}",activity.getId(),e.getMessage());
            }
        }
    }

}
