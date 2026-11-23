package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.ActivityType;
import com.krishu.caretracev2.ClientRole;
import com.krishu.caretracev2.CompletionStatus;
import com.krishu.caretracev2.CustomExceptions.InvalidStateException;
import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.UnauthorizedException;
import com.krishu.caretracev2.DTO.ActivityRequest;
import com.krishu.caretracev2.DTO.ActivityResponse;
import com.krishu.caretracev2.Day;
import com.krishu.caretracev2.Model.CareTaker;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Model.Routine;
import com.krishu.caretracev2.Model.RoutineCompletion;
import com.krishu.caretracev2.Repository.CareTakerRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import com.krishu.caretracev2.Repository.RoutineCompletionRepo;
import com.krishu.caretracev2.Repository.RoutineRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RoutineCompletionService {

    private final RoutineCompletionRepo routineCompletionRepo;
    private final PatientRepo patientRepo;
    private final CareTakerRepo careTakerRepo;
    private final RoutineRepo routineRepo;

    public RoutineCompletionService(RoutineCompletionRepo routineCompletionRepo,PatientRepo patientRepo,
                                    CareTakerRepo careTakerRepo,RoutineRepo routineRepo){
        this.routineCompletionRepo=routineCompletionRepo;
        this.patientRepo=patientRepo;
        this.careTakerRepo=careTakerRepo;
        this.routineRepo=routineRepo;
    }

    public ActivityResponse completeActivity(String activityId, ActivityRequest request
            , Authentication authentication){
        RoutineCompletion activity=routineCompletionRepo.findById(activityId).
                orElseThrow(()->new NotFoundException("Activity not found"));

        ClientRole role=checkAccess(activity.getPatientId(),authentication);
        if(activity.getCompletionStatus()== CompletionStatus.COMPLETED){
            throw new InvalidStateException("This activity is already completed");
        }
        activity.setCompletionStatus(CompletionStatus.COMPLETED);
        activity.setCompletedAt(LocalDateTime.now());
        activity.setCompletedByUserId(authentication.getName());
        activity.setCompletedByRole(role);
        if(role==ClientRole.CARETAKER){
            activity.setCareTakerConfirmed(true);
            activity.setConfirmAt(LocalDateTime.now());
        }
        if(request.getNote()!=null){
            activity.setNote(request.getNote());
        }
        return mapToResponse(routineCompletionRepo.save(activity));
    }

    public ActivityResponse confirmedActivity(String activityId,Authentication authentication){
        RoutineCompletion activity=routineCompletionRepo.findById(activityId).
                orElseThrow(()->new NotFoundException("Activity not found"));
        ClientRole role=checkAccess(activity.getPatientId(),authentication);
        if(role!=ClientRole.CARETAKER){
            throw new UnauthorizedException("Only caretaker can confirm an activity");
        }
        if(activity.getCompletionStatus()!=CompletionStatus.COMPLETED){
            throw new InvalidStateException("Only completed activity can be confirmed");
        }
        if(activity.getCareTakerConfirmed().equals(Boolean.TRUE)){
            throw new InvalidStateException("Activity is already confirmed");
        }
        activity.setCareTakerConfirmed(true);
        activity.setConfirmAt(LocalDateTime.now());
        return mapToResponse(routineCompletionRepo.save(activity));
    }

    public List<ActivityResponse> getMySchedule(LocalDate date,Authentication authentication){
        Patient patient=patientRepo.findByUserId(authentication.getName()).
                orElseThrow(()->new NotFoundException("Patient not found"));
        return buildSchedule(patient.getId(),(date!=null)?date:LocalDate.now());
    }

    public List<ActivityResponse> getSchedule(String patientId,LocalDate date,Authentication authentication){
        checkAccess(patientId,authentication);
        return buildSchedule(patientId,(date!=null)?date:LocalDate.now());
    }

    public List<ActivityResponse> getHistory(String patientId,ActivityType type,CompletionStatus status,LocalDate from
            ,LocalDate to,Authentication authentication){
        checkAccess(patientId,authentication);
        return query(patientId,status ,type,from,to,LocalDate.now().minusDays(30));
    }

    public List<ActivityResponse> getMyHistory(ActivityType type,CompletionStatus status,LocalDate from,LocalDate to
            ,Authentication authentication){
        Patient patient=patientRepo.findByUserId(authentication.getName()).
                orElseThrow(()->new NotFoundException("Patient not found"));
        return query(patient.getId(),status,type,from,to,LocalDate.now().minusDays(7));
    }

    private ClientRole checkAccess(String patientId, Authentication authentication){
        Optional<Patient> optionalPatient=patientRepo.findByUserId(authentication.getName());
        if(optionalPatient.isPresent()){
            if(!optionalPatient.get().getId().equals(patientId)){
                throw new UnauthorizedException("This dose does not belong to you");
            }
            return ClientRole.PATIENT;
        }
        CareTaker careTaker=careTakerRepo.findByUserId(authentication.getName())
                .orElseThrow(()->new NotFoundException("User not found"));
        Patient patient=patientRepo.findById(patientId).orElseThrow(()->new NotFoundException("Patient not found"));
        if(!patient.getCareTakerId().equals(careTaker.getId())){
            throw new UnauthorizedException("You are not authorized for this patient");
        }
        return ClientRole.CARETAKER;
    }

    private List<ActivityResponse> buildSchedule(String patientId, LocalDate date){
        LocalDateTime now=LocalDateTime.now();
        List<RoutineCompletion> routines=routineCompletionRepo.
                findByPatientIdAndScheduledAtBetween(patientId,date.atStartOfDay().minusSeconds(1)
                        ,date.plusDays(1).atStartOfDay());
        List<ActivityResponse> schedule=new ArrayList<>();
        Set<String> routinesWithRecord=new HashSet<>();
        for(RoutineCompletion record:routines){
            routinesWithRecord.add(record.getRoutineId());
            schedule.add(mapToResponse(record));
        }

        Day day= Day.from(date.getDayOfWeek());
        for(Routine routine:routineRepo.findByPatientId(patientId)){
            if(routine.getTime()==null || routine.getDays()==null || !routine.getDays().contains(day) ||
                    routinesWithRecord.contains(routine.getId())){
                continue;
            }
            LocalDateTime scheduledAt=date.atTime(routine.getTime());
            if(scheduledAt.isAfter(now)){
                schedule.add(mapUpcoming(routine,scheduledAt));
            }
        }
        schedule.sort(Comparator.comparing(ActivityResponse::getScheduledAt));
        return schedule;
    }

    private List<ActivityResponse> query(String patientId,CompletionStatus status,ActivityType type,LocalDate from
            ,LocalDate to,LocalDate defaultDate){
        LocalDate start=(from!=null)?from:defaultDate;
        LocalDate end=(to!=null)?to:LocalDate.now();

        return routineCompletionRepo.findByPatientIdAndScheduledAtBetween(patientId,
                        start.atStartOfDay().minusSeconds(1), end.plusDays(1).atStartOfDay())
                .stream()
                .filter(c->status==null || c.getCompletionStatus()==status)
                .filter(c->type==null || c.getActivityType()==type)
                .sorted(Comparator.comparing(RoutineCompletion::getScheduledAt).reversed())
                .map(this::mapToResponse)
                .toList();
    }

    private ActivityResponse mapToResponse(RoutineCompletion activity){
        ActivityResponse response=new ActivityResponse();
        response.setCompletionId(activity.getId());
        response.setRoutineId(activity.getRoutineId());
        response.setTitle(activity.getTitle());
        response.setActivityType(activity.getActivityType());
        response.setScheduledAt(activity.getScheduledAt());
        response.setStatus(activity.getCompletionStatus());
        response.setCompletedAt(activity.getCompletedAt());
        response.setCompletedByRole(activity.getCompletedByRole());
        response.setCareTakerConfirmed(activity.getCareTakerConfirmed());
        response.setConfirmedAt(activity.getConfirmAt());
        response.setNote(activity.getNote());
        return response;
    }

    private ActivityResponse mapUpcoming(Routine routine, LocalDateTime scheduledAt){
        ActivityResponse response=new ActivityResponse();
        response.setCompletionId(null);
        response.setRoutineId(routine.getId());
        response.setTitle(routine.getTitle());
        response.setActivityType(routine.getActivityType()!=null ? routine.getActivityType() : ActivityType.OTHER);
        response.setScheduledAt(scheduledAt);
        response.setStatus(CompletionStatus.UPCOMING);
        response.setCareTakerConfirmed(false);
        return response;
    }
}
