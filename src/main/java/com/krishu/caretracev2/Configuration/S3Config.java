package com.krishu.caretracev2.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {
    @Value("${s3.accesskey}")
    private String accessKey;
    @Value("${s3.secretkey}")
    private String secretKey;
    @Value("${s3.region}")
    private String region;

    @Bean
    public S3Client getS3Client(){
        return S3Client.builder().region(Region.of(region)).
                credentialsProvider(credentials()).build();
    }

    @Bean
    public S3Presigner getS3Presigner(){
        return S3Presigner.builder().region(Region.of(region))
                .credentialsProvider(credentials()).build();
    }
    
    private StaticCredentialsProvider credentials(){
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey,secretKey));
    }
}