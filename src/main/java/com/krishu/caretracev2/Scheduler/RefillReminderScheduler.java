package com.krishu.caretracev2.Scheduler;

import com.krishu.caretracev2.Model.Medication;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.NotificationType;
import com.krishu.caretracev2.Repository.MedicationRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import com.krishu.caretracev2.Service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class RefillReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(RefillReminderScheduler.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    private final MedicationRepo medicationRepo;
    private final PatientRepo patientRepo;
    private final NotificationService notificationService;

    public RefillReminderScheduler(MedicationRepo medicationRepo,PatientRepo patientRepo
            ,NotificationService notificationService){
        this.medicationRepo=medicationRepo;
        this.patientRepo=patientRepo;
        this.notificationService=notificationService;
    }

    @Scheduled(cron="0 0 9 * * *")
    public void checkRefill(){
        LocalDate today= LocalDate.now();
        List<Medication> refillMedication=medicationRepo.findByRefillDueDateLessThanEqual(today);

        for(Medication medication:refillMedication){
            try{

                if(!medication.isActive(today)){
                    continue;
                }

                if(medication.getRefillDueDate().equals(medication.getRefillAlertFor())){
                    continue;
                }

                Patient patient=patientRepo.findById(medication.getId()).orElse(null);
                if(patient==null){
                    continue;
                }

                String message = medication.getName() + " needs a refill by "
                        + medication.getRefillDueDate().format(DATE_FORMAT);
                notificationService.createNotification(patient.getCareTakerId(), patient.getId(),
                        null, NotificationType.MEDICATION_REFILL, message);

                medication.setRefillAlertFor(medication.getRefillDueDate());
                medicationRepo.save(medication);
            }catch(RuntimeException e){
                log.warn("Could not process refill alert for medication {}: {}", medication.getId(), e.getMessage());
            }
        }
    }
}
