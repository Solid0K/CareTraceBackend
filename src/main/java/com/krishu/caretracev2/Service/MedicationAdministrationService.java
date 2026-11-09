package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.AdministrationStatus;
import com.krishu.caretracev2.ClientRole;
import com.krishu.caretracev2.CustomExceptions.AlreadyAdministrationException;
import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.UnauthorizedException;
import com.krishu.caretracev2.DTO.MarkTakenRequest;
import com.krishu.caretracev2.DTO.MedicationAdministrationResponse;
import com.krishu.caretracev2.Model.CareTaker;
import com.krishu.caretracev2.Model.MedicationAdministration;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Repository.CareTakerRepo;
import com.krishu.caretracev2.Repository.MedicationAdministrationRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MedicationAdministrationService {

    private final MedicationAdministrationRepo medicationAdminRepo;
    private final PatientRepo patientRepo;
    private final CareTakerRepo careTakerRepo;

    public MedicationAdministrationService(MedicationAdministrationRepo medicationAdminRepo,
                                           PatientRepo patientRepo,CareTakerRepo careTakerRepo){
        this.medicationAdminRepo=medicationAdminRepo;
        this.patientRepo=patientRepo;
        this.careTakerRepo=careTakerRepo;
    }

    public MedicationAdministrationResponse markAsTaken(String administrationId,
                                                        Authentication authentication, MarkTakenRequest request){
        MedicationAdministration medicationAdmin=medicationAdminRepo.findById(administrationId).
                orElseThrow(()->new NotFoundException("Record not found"));
        ClientRole role=checkAccess(medicationAdmin.getPatientId(),authentication);
        if(medicationAdmin.getStatus()==AdministrationStatus.TAKEN){
            throw new AlreadyAdministrationException("This dose is already marked as taken");
        }
        medicationAdmin.setStatus(AdministrationStatus.TAKEN);
        medicationAdmin.setTakenAt(LocalDateTime.now());
        medicationAdmin.setAdministrationUserId(authentication.getName());
        medicationAdmin.setAdministrationByRole(role);
        if(request.getNote()!=null){
            medicationAdmin.setNote(request.getNote());
        }
        return mapToResponse(medicationAdminRepo.save(medicationAdmin));
    }

    public List<MedicationAdministrationResponse> getHistory(String patientId,AdministrationStatus status,
                                                             String medicationId,LocalDate from,
                                                             LocalDate to,Authentication authentication){
        checkAccess(patientId,authentication);
        return query(patientId,status,medicationId,from,to,LocalDate.now().minusDays(30));
    }

    public List<MedicationAdministrationResponse> getMyDosage(AdministrationStatus status,LocalDate to
            ,LocalDate from,Authentication authentication){
        Patient patient=patientRepo.findByUserId(authentication.getName()).
                orElseThrow(()->new NotFoundException("Patient not found"));
        return query(patient.getId(),status,null,from,to,LocalDate.now());
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

    private List<MedicationAdministrationResponse> query(String patientId, AdministrationStatus status,
                                                         String medicationId
            , LocalDate from, LocalDate to, LocalDate defaultFrom){
        LocalDate start=(from!=null)?from:defaultFrom;
        LocalDate end=(to!=null)?to:LocalDate.now();

        LocalDateTime startTime=start.atStartOfDay().minusSeconds(1);
        LocalDateTime endTime=end.plusDays(1).atStartOfDay();

        return medicationAdminRepo.
                findByPatientIdAndScheduledAtBetweenOrderByScheduledAtDesc(patientId,startTime,endTime).
                stream().filter(a->status==null || a.getStatus()==status).
                filter(a->medicationId==null || a.getMedicationId().equals(medicationId)).
                map(this::mapToResponse).toList();
    }


    private MedicationAdministrationResponse mapToResponse(MedicationAdministration medicationAdmin){
        MedicationAdministrationResponse response=new MedicationAdministrationResponse();
        response.setId(medicationAdmin.getId());
        response.setDosage(medicationAdmin.getDosage());
        response.setMedicationId(medicationAdmin.getMedicationId());
        response.setMedicationName(medicationAdmin.getMedicationName());
        response.setNote(medicationAdmin.getNote());
        response.setScheduledAt(medicationAdmin.getScheduledAt());
        response.setTakenAt(medicationAdmin.getTakenAt());
        response.setStatus(medicationAdmin.getStatus());
        return response;
    }
}
