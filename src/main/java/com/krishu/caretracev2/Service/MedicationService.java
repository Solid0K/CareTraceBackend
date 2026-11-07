package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.NotRelatedException;
import com.krishu.caretracev2.CustomExceptions.UnauthorizedException;
import com.krishu.caretracev2.DTO.CareTakerPatientPair;
import com.krishu.caretracev2.DTO.MedicationRequest;
import com.krishu.caretracev2.DTO.MedicationResponse;
import com.krishu.caretracev2.Model.CareTaker;
import com.krishu.caretracev2.Model.Medication;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Repository.CareTakerRepo;
import com.krishu.caretracev2.Repository.MedicationRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepo medicationRepo;
    private final PatientRepo patientRepo;
    private final CareTakerRepo careTakerRepo;

    public MedicationService(MedicationRepo medicationRepo, PatientRepo patientRepo, CareTakerRepo careTakerRepo) {
        this.medicationRepo = medicationRepo;
        this.patientRepo = patientRepo;
        this.careTakerRepo = careTakerRepo;
    }

    public MedicationResponse createMedication(MedicationRequest request, String patientId,
                                               Authentication authentication){
        CareTakerPatientPair pair=careTakerAndPatient(authentication,patientId);
        CareTaker careTaker=pair.getCareTaker();
        Patient patient=pair.getPatient();
        Medication medication=new Medication();
        medication.setName(request.getName());
        medication.setDosage(request.getDosage());
        medication.setEndDate(request.getEndDate());
        medication.setFrequency(request.getFrequency());
        medication.setInstructions(request.getInstructions());
        medication.setStartDate(request.getStartDate());
        medication.setPatientId(patient.getId());
        medication.setPrescribingDoctor(request.getPrescribingDoctor());
        Medication savedMedication=medicationRepo.save(medication);
        return mapToMedicationResponse(savedMedication);
    }

    public List<MedicationResponse> getPatientMedications(String patientId,Authentication authentication){
        getAuthorizedPatient(patientId,authentication);
        List<Medication> medications=medicationRepo.findByPatientId(patientId);
        return medications.stream().map(this::mapToMedicationResponse).toList();
    }

    public MedicationResponse updateMedication(MedicationRequest request,String medicationId,String patientId,Authentication authentication){
        CareTakerPatientPair pair=careTakerAndPatient(authentication,patientId);
        CareTaker careTaker=pair.getCareTaker();
        Patient patient=pair.getPatient();
        Medication medication=medicationRepo.findById(medicationId).orElseThrow(()->new NotFoundException("Medication not found"));
        if(!medication.getPatientId().equals(patient.getId())){
            throw new NotRelatedException("Medication dont belong the Patient");
        }
        medication.setName(request.getName());
        medication.setDosage(request.getDosage());
        medication.setEndDate(request.getEndDate());
        medication.setFrequency(request.getFrequency());
        medication.setInstructions(request.getInstructions());
        medication.setStartDate(request.getStartDate());
        medication.setPrescribingDoctor(request.getPrescribingDoctor());
        Medication savedMedication=medicationRepo.save(medication);
        return mapToMedicationResponse(savedMedication);
    }

    public void deleteMedication(String medicationId,String patientId,Authentication authentication){
        CareTakerPatientPair pair=careTakerAndPatient(authentication,patientId);
        CareTaker careTaker=pair.getCareTaker();
        Patient patient=pair.getPatient();
        Medication medication=medicationRepo.findById(medicationId).orElseThrow(()->new NotFoundException("Medication not found"));
        if(!medication.getPatientId().equals(patient.getId())){
            throw new NotRelatedException("Medication dont belong the Patient");
        }
        medicationRepo.delete(medication);
    }

    public List<MedicationResponse> getMedicationForPatient(Authentication authentication) {
        String patientUserId=authentication.getName();
        Patient patient=patientRepo.findByUserId(patientUserId).orElseThrow(()->new NotFoundException("Patient not found"));
        return medicationRepo.findByPatientId(patient.getId()).stream().map(this::mapToMedicationResponse).toList();
    }

    public List<MedicationResponse> getPatientMedication(String patientId,Authentication authentication,
                                                         Boolean active){
        getAuthorizedPatient(patientId,authentication);
        LocalDate today=LocalDate.now();
        return medicationRepo.findByPatientId(patientId).stream().filter(m->active==null ||
                isActive(m,today)==active).map(this::mapToMedicationResponse).toList();
    }

    private boolean isActive(Medication medication, LocalDate today){
        boolean startedAt=medication.getStartDate()==null || !medication.getStartDate().isAfter(today);
        boolean endAt=medication.getEndDate()==null || !medication.getEndDate().isBefore(today);
        return startedAt && endAt;
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

    private MedicationResponse mapToMedicationResponse(Medication medication){
        MedicationResponse response=new MedicationResponse();
        response.setDosage(medication.getDosage());
        response.setEndDate(medication.getEndDate());
        response.setFrequency(medication.getFrequency());
        response.setInstructions(medication.getInstructions());
        response.setName(medication.getName());
        response.setStartDate(medication.getStartDate());
        response.setId(medication.getId());
        response.setPrescribingDoctor(medication.getPrescribingDoctor());
        return response;
    }
}
