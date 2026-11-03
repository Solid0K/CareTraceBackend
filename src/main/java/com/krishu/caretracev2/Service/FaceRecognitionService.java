package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.DTO.FaceRecognitionResponse;
import com.krishu.caretracev2.Model.ImportantPerson;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Repository.ImportantPersonRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CompareFacesRequest;
import software.amazon.awssdk.services.rekognition.model.CompareFacesResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;

import java.util.List;

@Service
public class FaceRecognitionService {

    private final RekognitionClient rekognitionClient;
    private final ImportantPersonRepo importantPersonRepo;
    private final PatientRepo patientRepo;
    @Value("${s3.bucket}")
    private String s3Bucket;

    public FaceRecognitionService(RekognitionClient rekognitionClient,ImportantPersonRepo importantPersonRepo,PatientRepo patientRepo){
        this.rekognitionClient=rekognitionClient;
        this.importantPersonRepo=importantPersonRepo;
        this.patientRepo=patientRepo;
    }

    public Float compareImages(Image sourceImage,Image newImagePhoto){
        CompareFacesRequest request= CompareFacesRequest.builder().sourceImage(sourceImage).
                targetImage(newImagePhoto).similarityThreshold(90F).build();
        CompareFacesResponse response=rekognitionClient.compareFaces(request);
        if(response.faceMatches().isEmpty()){
            return 0F;
        }
        return response.faceMatches().get(0).similarity();
    }

    public FaceRecognitionResponse recognisePerson(Authentication authentication , byte[] newImage){
        Patient patient=patientRepo.findByUserId(authentication.getName()).orElseThrow(()->new NotFoundException("Patient not found"));
        List<ImportantPerson> persons=importantPersonRepo.findByPatientId(patient.getId());
        Float highestSimilarity=0F;
        ImportantPerson bestMatchPerson=null;
        for(ImportantPerson person:persons){
            if(person.getPhoto()==null){
                continue;
            }
            Float similarity=compareImages(person.getPhoto(),newImage);
            if(similarity>highestSimilarity){
                highestSimilarity=similarity;
                bestMatchPerson=person;
            }
        }
        if(highestSimilarity<90F){
            return null;
        }

        FaceRecognitionResponse response=new FaceRecognitionResponse(bestMatchPerson.getName(),bestMatchPerson.getRelation(),highestSimilarity);
        return response;
    }

    private Image fromByteToImage(byte[] image){
        return Image.builder().bytes(SdkBytes.fromByteArray(image)).build();
    }

    private Image getImageFromKey(String key){
        return Image.builder().s3Object(S3Object.builder().
                bucket(s3Bucket).name(key).build()).build();
    }
}
