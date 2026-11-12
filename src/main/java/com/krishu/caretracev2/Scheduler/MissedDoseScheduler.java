package com.krishu.caretracev2.Scheduler;

import com.krishu.caretracev2.AdministrationStatus;
import com.krishu.caretracev2.Model.MedicationAdministration;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.NotificationType;
import com.krishu.caretracev2.Repository.MedicationAdministrationRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
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
public class MissedDoseScheduler {

    private static final Logger log = (Logger) LoggerFactory.getLogger(MissedDoseScheduler.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final MedicationAdministrationRepo medicationAdminRepo;
    private final PatientRepo patientRepo;
    private final NotificationService notificationService;

    @Value("${medication.missed.grace.minutes}")
    private long graceMinute;

    public MissedDoseScheduler(MedicationAdministrationRepo medicationAdminRepo,PatientRepo patientRepo
            ,NotificationService notificationService){
        this.medicationAdminRepo=medicationAdminRepo;
        this.patientRepo=patientRepo;
        this.notificationService=notificationService;
    }

    @Scheduled(fixedRate=60000)
    public void markMissedDose(){
        LocalDateTime cutOff=LocalDateTime.now().minusMinutes(graceMinute);
        List<MedicationAdministration> missedDosage=medicationAdminRepo.
                findByStatusAndScheduledAtBefore(AdministrationStatus.MISSED,cutOff);
            for(MedicationAdministration dose:missedDosage){
                try{
                    Patient patient=patientRepo.findById(dose.getPatientId()).orElse(null);
                    if(patient!=null){
                        String message = "Missed dose: " + dose.getMedicationName() + " (" + dose.getDosage()
                                + ") was due at " + dose.getScheduledAt().format(TIME_FORMAT);
                        notificationService.createNotification(patient.getCareTakerId(), patient.getId(),
                                null, NotificationType.MEDICATION_MISSED, message);
                    }
                    dose.setStatus(AdministrationStatus.MISSED);
                    medicationAdminRepo.save(dose);
                }catch(RuntimeException e){
                    log.warn("Could not mark dose {} as missed: {}", dose.getId(), e.getMessage());
                }
            }
    }
}
