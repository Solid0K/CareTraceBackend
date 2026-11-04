package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.NotRelatedException;
import com.krishu.caretracev2.CustomExceptions.UnauthorizedException;
import com.krishu.caretracev2.DTO.ImportantPersonRequest;
import com.krishu.caretracev2.DTO.ImportantPersonResponse;
import com.krishu.caretracev2.DTO.PersonForFlashCard;
import com.krishu.caretracev2.Model.CareTaker;
import com.krishu.caretracev2.Model.ImportantPerson;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Repository.CareTakerRepo;
import com.krishu.caretracev2.Repository.ImportantPersonRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImportantPersonService {

    private final ImportantPersonRepo importantPersonRepo;
    private final CareTakerRepo careTakerRepo;
    private final PatientRepo patientRepo;
    private final PhotoStorageService photoStorageService;

    public ImportantPersonService(ImportantPersonRepo importantPersonRepo, CareTakerRepo careTakerRepo
            , PatientRepo patientRepo,PhotoStorageService photoStorageService) {
        this.importantPersonRepo = importantPersonRepo;
        this.careTakerRepo = careTakerRepo;
        this.patientRepo = patientRepo;
        this.photoStorageService=photoStorageService;
    }

    public ImportantPersonResponse createImportantPerson(ImportantPersonRequest request
            , String patientId, MultipartFile photo, Authentication authentication) throws IOException {
        CareTaker careTaker=careTakerRepo.findByUserId(authentication.getName())
                .orElseThrow(()->new NotFoundException("CareTaker not found"));
        Patient patient=patientRepo.findById(patientId).orElseThrow(()->new NotFoundException("Patient not found"));
        if(!patient.getCareTakerId().equals(careTaker.getId())){
            throw new UnauthorizedException("You are not authorize to access this patient");
        }
        ImportantPerson importantPerson=new ImportantPerson();
        importantPerson.setName(request.getName());
        importantPerson.setRelation(request.getRelation());
        importantPerson.setPhoneNo(request.getPhoneNo());
        importantPerson.setPatientId(patient.getId());
        if(photo!=null && !photo.isEmpty()){
            importantPerson.setPhotoKey(photoStorageService.
                    uploadPhoto(photo, "patients/"+patientId+"/people/"));
        }
        ImportantPerson savedImportantPerson=importantPersonRepo.save(importantPerson);
        return mapToImportantPersonResponse(savedImportantPerson);
    }

    public List<ImportantPersonResponse> getImportantPersons(String patientId,Authentication authentication){
        CareTaker careTaker=getCareTaker(authentication);
        Patient patient=patientRepo.findById(patientId).orElseThrow(()->new NotFoundException("Patient not found"));
        if(!patient.getCareTakerId().equals(careTaker.getId())){
            throw new UnauthorizedException("You are not authorize to access this patient");
        }
        List<ImportantPerson> importantPersons=importantPersonRepo.findByPatientId(patientId);
        return importantPersons.stream().map(this::mapToImportantPersonResponse).toList();
    }

    public ImportantPersonResponse updatePerson(ImportantPersonRequest request
            ,String patientId,String importantPersonId
            ,Authentication authentication,MultipartFile photo) throws IOException {
        CareTaker careTaker=getCareTaker(authentication);
        Patient patient=patientRepo.findById(patientId).orElseThrow(()->new NotFoundException("Patient not found"));
        if(!patient.getCareTakerId().equals(careTaker.getId())){
            throw new UnauthorizedException("You are not authorize to access this patient");
        }
        ImportantPerson importantPerson=importantPersonRepo.findById(importantPersonId)
                .orElseThrow(()->new NotFoundException("Person not found"));
        importantPerson.setName(request.getName());
        importantPerson.setRelation(request.getRelation());
        importantPerson.setPhoneNo(request.getPhoneNo());
        String oldKey=importantPerson.getPhotoKey();
        if(photo!=null && !photo.isEmpty()){
            importantPerson.setPhotoKey(photoStorageService.
                    uploadPhoto(photo,"patients/"+patientId+"/people/"));
        }
        ImportantPerson savedPerson=importantPersonRepo.save(importantPerson);
        if(photo!=null && !photo.isEmpty()){
            photoStorageService.deletePhoto(oldKey);
        }
        return mapToImportantPersonResponse(savedPerson);
    }

    public void deletePerson(String importantPersonId,String patientId,Authentication authentication){
        CareTaker careTaker=getCareTaker(authentication);
        Patient patient=patientRepo.findById(patientId).orElseThrow(()->new NotFoundException("Patient not found"));
        if(!patient.getCareTakerId().equals(careTaker.getId())){
            throw new UnauthorizedException("You are not authorize to access this patient");
        }
        ImportantPerson person=importantPersonRepo.findById(patientId).orElseThrow(()
                ->new NotFoundException("Person not found"));
        if(!person.getPatientId().equals(patientId)){
            throw new NotRelatedException("Person and Patient are not related");
        }
        importantPersonRepo.delete(person);
        photoStorageService.deletePhoto(person.getPhotoKey());
    }

    public List<ImportantPersonResponse> getPatientImportantPerson(Authentication authentication) {
        String patientUserId=authentication.getName();
        Patient patient=patientRepo.findByUserId(patientUserId).orElseThrow(()
                ->new NotFoundException("Patient not found"));
        return importantPersonRepo.findByPatientId(patient.getId()).stream().
                map(this::mapToImportantPersonResponse).toList();
    }

    public List<ImportantPerson> getPersonsPhotos(String patientId){;
        List<ImportantPerson> importantPersons=importantPersonRepo.findByPatientId(patientId);
        return importantPersons.stream().filter(person->person.getPhotoKey()!=null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ImportantPersonResponse mapToImportantPersonResponse(ImportantPerson importantPerson){
        ImportantPersonResponse response=new ImportantPersonResponse();
        response.setName(importantPerson.getName());
        response.setId(importantPerson.getId());
        response.setPhoneNo(importantPerson.getPhoneNo());
        response.setRelation(importantPerson.getRelation());
        response.setPhotoUrl(photoStorageService.photoUrl(importantPerson.getPhotoKey()));
        return response;
    }

    private CareTaker getCareTaker(Authentication authentication){
        return careTakerRepo.findByUserId(authentication.getName()).orElseThrow(()
                ->new NotFoundException("CareTaker not found"));
    }
}