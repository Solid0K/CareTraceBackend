package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class PhotoStorageService {

    private static final long MAX_FILE_SIZE_BYTE=2L*1024*1024;
    private static final Logger log = LoggerFactory.getLogger(PhotoStorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${s3.bucket}")
    private String s3Bucket;
    @Value("${s3.url.expiry.minutes}")
    private long expiryMinutes;

    public PhotoStorageService(S3Client s3Client,S3Presigner s3Presigner){
        this.s3Client=s3Client;
        this.s3Presigner=s3Presigner;
    }

    public String uploadPhoto(MultipartFile file, String prefix){
        if(file==null || file.isEmpty()){
            throw new InvalidFileException("Photo is required");
        }
        if(file.getSize()>MAX_FILE_SIZE_BYTE){
            throw new InvalidFileException("File is too big");
        }

        byte[] photoBytes;
        try{
            photoBytes=file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String contentType=detectContentType(photoBytes);
        if(contentType==null){
            throw new InvalidFileException("Only jpeg or png allowed");
        }

        String key=prefix+ UUID.randomUUID();
        s3Client.putObject(PutObjectRequest.builder().bucket(s3Bucket).
                key(key).contentType(contentType).build(), RequestBody.fromBytes(photoBytes));
        return key;
    }

    public void deletePhoto(String key){
        if(key==null){
            return;
        }
        try{
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3Bucket).key(key).build());
        }catch(S3Exception e){
            log.warn("Could not delete S3 object {}: {}", key, e.getMessage());
        }
    }

    public String photoUrl(String key){
        if(key==null){
            return null;
        }
        GetObjectRequest getObjectRequest=GetObjectRequest.builder()
                .bucket(s3Bucket).key(key).build();
        
        GetObjectPresignRequest presignRequest=GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .getObjectRequest(getObjectRequest).build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private String detectContentType(byte[] b){
        if (b.length >= 3
                && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "image/png";
        }
        return null;
    }
}
