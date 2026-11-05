package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.UnauthorizedException;
import com.krishu.caretracev2.DTO.ImportantPlaceRequest;
import com.krishu.caretracev2.DTO.ImportantPlaceResponse;
import com.krishu.caretracev2.Model.CareTaker;
import com.krishu.caretracev2.Model.ImportantPlace;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Repository.CareTakerRepo;
import com.krishu.caretracev2.Repository.ImportantPlaceRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ImportantPlaceService {

    private final ImportantPlaceRepo importantPlaceRepo;
    private final CareTakerRepo careTakerRepo;
    private final PatientRepo patientRepo;
    private final PhotoStorageService photoStorageService;

    public ImportantPlaceService(ImportantPlaceRepo placeRepo, CareTakerRepo careTakerRepo
            , PatientRepo patientRepo,PhotoStorageService photoStorageService){
        this.importantPlaceRepo=placeRepo;
        this.careTakerRepo=careTakerRepo;
        this.patientRepo=patientRepo;
        this.photoStorageService=photoStorageService;
    }


    public ImportantPlaceResponse addImportantPlace(Authentication authentication, ImportantPlaceRequest request
            , String patientId, MultipartFile photoFile) throws IOException {
        if(photoFile==null || photoFile.isEmpty()){
            throw new IllegalArgumentException("PhotoFile is Empty");
        }

        String contentType=photoFile.getContentType();
        if(contentType==null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))){
            throw new IllegalArgumentException("Omly png or jpeg is allowed");
        }

        byte[] photo=photoFile.getBytes();
        CareTaker careTaker=careTakerRepo.findByUserId(authentication.getName()).
                orElseThrow(()->new NotFoundException("CareTaker not found"));
        if(!careTaker.getPatientIds().contains(patientId)){
            throw new UnauthorizedException("You dont have authority for this patient");
        }
        ImportantPlace place=new ImportantPlace();
        place.setDescription(request.getDescription());
        place.setName(request.getName());
        place.setPatientId(patientId);
        place.setPhotoKey(photoStorageService.uploadPhoto(photoFile,"patients/" + patientId + "/places/"));
        try{
            return mapToPlaceResponse(importantPlaceRepo.save(place));
        }catch(RuntimeException e){
            photoStorageService.deletePhoto(place.getPhotoKey());
            throw e;
        }
    }

    public List<ImportantPlaceResponse> getAllImportantPlaces(Authentication authentication
            , String patientId) {
        String userId=authentication.getName();

        Optional<Patient> patient=patientRepo.findByUserId(userId);
        if(patient.isPresent() && patient.get().getId().equals(patientId)){
            return importantPlaceRepo.findByPatientId(patientId)
                    .stream().map(this::mapToPlaceResponse).toList();
        }

        CareTaker careTaker=careTakerRepo.findByUserId(userId).
                orElseThrow(()->new UnauthorizedException("Access denied"));
        if(!careTaker.getPatientIds().contains(patientId)){
            throw new UnauthorizedException("Access denied");
        }
        return importantPlaceRepo.findByPatientId(patientId)
                .stream().map(this::mapToPlaceResponse).toList();
    }

    public ImportantPlaceResponse getPlace(Authentication authentication, String placeId) {
        ImportantPlace place=importantPlaceRepo.findById(placeId).
                orElseThrow(()->new NotFoundException("Place not found"));

        String userId=authentication.getName();

        Optional<Patient> patient=patientRepo.findByUserId(userId);
        if(patient.isPresent() && patient.get().getId().equals(place.getPatientId())){
            return mapToPlaceResponse(place);
        }

        CareTaker careTaker=careTakerRepo.findByUserId(userId).
                orElseThrow(()->new UnauthorizedException("Access denied"));
        if(!careTaker.getPatientIds().contains(place.getPatientId())){
            throw new UnauthorizedException("Access denied");
        }

        return mapToPlaceResponse(place);
    }

    public void deleteImportantPlace(Authentication authentication, String placeId) {
        CareTaker careTaker=careTakerRepo.findByUserId(authentication.getName())
                .orElseThrow(()->new NotFoundException("CareTaker not found"));
        ImportantPlace place=importantPlaceRepo.findById(placeId).
                orElseThrow(()->new NotFoundException("Place not found"));
        if(!careTaker.getPatientIds().contains(place.getPatientId())){
            throw new UnauthorizedException("You dont have authority for this patient");
        }
        importantPlaceRepo.delete(place);
        photoStorageService.deletePhoto(place.getPhotoKey());
    }

    private ImportantPlaceResponse mapToPlaceResponse(ImportantPlace place){
        ImportantPlaceResponse response=new ImportantPlaceResponse();
        response.setId(place.getId());
        response.setName(place.getName());
        response.setDescription(place.getDescription());
        response.setPatientId(place.getPatientId());
        response.setPhotoUrl(photoStorageService.photoUrl(place.getPhotoKey()));
        return response;
    }
}
