package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.NotRelatedException;
import com.krishu.caretracev2.CustomExceptions.UnauthorizedException;
import com.krishu.caretracev2.DTO.CareTakerPatientPair;
import com.krishu.caretracev2.DTO.ReminderRequest;
import com.krishu.caretracev2.DTO.ReminderResponse;
import com.krishu.caretracev2.Model.*;
import com.krishu.caretracev2.ReminderType;
import com.krishu.caretracev2.Repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderService {

    private final CareTakerRepo careTakerRepo;
    private final PatientRepo patientRepo;
    private final ReminderRepo reminderRepo;
    private final MedicationRepo medicationRepo;
    private final RoutineRepo routineRepo;

    public ReminderService(CareTakerRepo careTakerRepo, PatientRepo patientRepo, ReminderRepo reminderRepo, MedicationRepo medicationRepo, RoutineRepo routineRepo) {
        this.careTakerRepo = careTakerRepo;
        this.patientRepo = patientRepo;
        this.reminderRepo = reminderRepo;
        this.medicationRepo = medicationRepo;
        this.routineRepo = routineRepo;
    }

    public ReminderResponse createReminder(ReminderRequest request, String patientId, Authentication authentication){
        CareTakerPatientPair pair=careTakerAndPatient(authentication,patientId);
        Reminder reminder=new Reminder();
        reminder.setDate(request.getDate());
        reminder.setPatientId(patientId);
        reminder.setTime(request.getTime());
        reminder.setType(request.getType());
        validateReminder(patientId,request);
        reminder.setMedicationId(request.getMedicationId());
        reminder.setRoutineId(request.getRoutineId());
        Reminder savedReminder=reminderRepo.save(reminder);
        return mapToReminderResponse(savedReminder);
    }

    public List<ReminderResponse> getPatientReminders(String patientId,Authentication authentication){
        getAuthorizedPatient(patientId,authentication);
        if(!patientRepo.existsById(patientId)){
            throw new NotFoundException("Patient not found");
        }
        List<Reminder> reminders=reminderRepo.findByPatientId(patientId);
        return reminders.stream().map(this::mapToReminderResponse).toList();
    }

    public ReminderResponse updateReminder(String reminderId,String patientId,ReminderRequest request,Authentication authentication){
        CareTakerPatientPair pair=careTakerAndPatient(authentication,patientId);
        Reminder reminder=reminderRepo.findById(reminderId).orElseThrow(()->new NotFoundException("Reminder not found"));
        if(!reminder.getPatientId().equals(patientId)){
            throw new NotRelatedException("Reminder does not belong to this Patient");
        }
        reminder.setDate(request.getDate());
        reminder.setTime(request.getTime());
        reminder.setType(request.getType());
        validateReminder(patientId,request);
        reminder.setMedicationId(request.getMedicationId());
        reminder.setRoutineId(request.getRoutineId());
        Reminder savedReminder=reminderRepo.save(reminder);
        return mapToReminderResponse(savedReminder);
    }

    public void deleteReminder(String reminderId,String patientId,Authentication authentication){
        CareTakerPatientPair pair=careTakerAndPatient(authentication,patientId);
        Reminder reminder=reminderRepo.findById(reminderId).orElseThrow(()->new NotFoundException("Reminder not found"));
        if(!reminder.getPatientId().equals(patientId)){
            throw new NotRelatedException("Reminder does not belong to this Patient");
        }
        reminderRepo.delete(reminder);
    }

    public List<ReminderResponse> getReminderForPatient(Authentication authentication) {
        String patientUserId=authentication.getName();
        Patient patient=patientRepo.findByUserId(patientUserId).orElseThrow(()->new NotFoundException("Patient not found"));
        return reminderRepo.findByPatientId(patient.getId()).stream().map(this::mapToReminderResponse).toList();
    }

    private void validateReminder(String patientId, ReminderRequest request){
        if(request.getType()== ReminderType.MEDICATION){
            if(request.getMedicationId()==null){
                throw new IllegalArgumentException("Medication id should not be null");
            }
            if(request.getRoutineId()!=null){
                throw new IllegalArgumentException("Routine id shouldn't be provided for a medication reminder");
            }
            Medication medication=medicationRepo.findById(request.getMedicationId()).orElseThrow(()->new NotFoundException("Medication not found"));
            if(!medication.getPatientId().equals(patientId)){
                throw new NotRelatedException("Medication does not belong to this Patient");
            }
            if(request.getTime()==null){
                throw new IllegalArgumentException("Time is required for a medication reminder");
            }
        }else if(request.getType()==ReminderType.ROUTINE){
            if (request.getRoutineId() == null) {
                throw new IllegalArgumentException("Routine ID is required for routine reminder");
            }
            if (request.getMedicationId() != null) {
                throw new IllegalArgumentException("Medication ID should not be provided for routine reminder");
            }
            Routine routine = routineRepo.findById(request.getRoutineId()).orElseThrow(() -> new NotFoundException("Routine not found"));
            if (!routine.getPatientId().equals(patientId)) {
                throw new NotRelatedException("Routine does not belong to this patient");
            }
        }else if (request.getType() == ReminderType.CUSTOM) {
            if (request.getMedicationId() != null || request.getRoutineId() != null) {
                throw new IllegalArgumentException("Custom reminder cannot reference medication or routine");
            }
        }
    }

    private CareTakerPatientPair careTakerAndPatient(Authentication authentication, String patientId){
        CareTaker careTaker=careTakerRepo.findByUserId(authentication.getName()).
                orElseThrow(()->new NotFoundException("CareTaker not found"));
        Patient patient=patientRepo.findById(patientId).orElseThrow(()->new NotFoundException("Patient not found"));
        if(!patient.getCareTakerId().equals(careTaker.getId())){
            throw new UnauthorizedException("You are not authorized for this Patient");
        }
        CareTakerPatientPair pair=new CareTakerPatientPair(careTaker,patient);
        return pair;
    }

    private Patient getAuthorizedPatient(String patientId, Authentication authentication) {
        CareTaker careTaker = careTakerRepo.findByUserId(authentication.getName()).orElseThrow(() -> new NotFoundException("CareTaker not found"));
        Patient patient = patientRepo.findById(patientId).orElseThrow(() -> new NotFoundException("Patient not found"));
        if (!patient.getCareTakerId().equals(careTaker.getId())) {
            throw new UnauthorizedException("You are not authorized for this patient");
        }
        return patient;
    }

    private ReminderResponse mapToReminderResponse(Reminder reminder){
        ReminderResponse response=new ReminderResponse();
        response.setId(reminder.getId());
        response.setDate(reminder.getDate());
        response.setTime(reminder.getTime());
        response.setType(reminder.getType());
        return response;
    }
}
