package com.quantumitllc.spmedia.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class AwsConfig {

  private final AwsProperties properties;

  @Bean
  public AmazonS3 initS3Client() {
    AWSCredentials credentials = new BasicAWSCredentials(properties.getAccessKey(),
        properties.getSecretKey());
    return AmazonS3ClientBuilder.standard()
        .withRegion(Regions.fromName(properties.getRegion()))
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();
  }

}

