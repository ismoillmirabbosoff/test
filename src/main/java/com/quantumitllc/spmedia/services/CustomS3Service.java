package com.quantumitllc.spmedia.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.io.Files;
import com.quantumitllc.spmedia.config.AwsProperties;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomS3Service {

  private static final String UPLOAD_ERROR = "Could not upload the file";
  private static final String AWS_UPLOAD_ERROR = "Error happened during aws upload";
  private static final String TITLE = "title";
  private static final String TYPE = "type";
  private static final String UNTITLED = "untitled";

  private final AmazonS3 amazonS3;
  private final AwsProperties awsProperties;

  public String uploadGivenFile(MultipartFile file, String bucketName, String folder) {
    try {
      String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse(UNTITLED);
      var inputStream = file.getInputStream();
      String ext = Files.getFileExtension(originalName);
      Map<String, String> extra = new HashMap<>();
      extra.put(TYPE, URLEncoder.encode(originalName, StandardCharsets.UTF_8));
      extra.put(TITLE, ext);

      String key = prefixedKey(UUID.randomUUID() + "." + ext, folder);
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(file.getContentType());
      metadata.setContentLength(file.getSize());
      metadata.setUserMetadata(extra);
      PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream,
          metadata);

      log.info("Uploading file with original name: {} and key {}", originalName, key);
      amazonS3.putObject(putObjectRequest);
      return key;
    } catch (IOException e) {
      log.error("Could not read file content", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, UPLOAD_ERROR);
    } catch (AmazonServiceException e) {
      log.error("Error happened during aws upload", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, AWS_UPLOAD_ERROR);
    }
  }

  private String prefixedKey(@Nonnull String key, @Nullable String prefix) {
      if (prefix != null) {
          return prefix + "/" + key;
      } else {
          return key;
      }
  }

  public void removeObject(String key, String bucketName) {
    amazonS3.deleteObject(bucketName, key);
  }

  public void removeMultipleObjects(List<String> keys, String bucketName) {
    if (keys.size() < 1) {
      return;
    }
    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
    deleteObjectsRequest.withKeys(keys.toArray(new String[0]));
    try {
      amazonS3.deleteObjects(deleteObjectsRequest);
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

  public S3Object fetchObject(String key, String bucketName) {
    try {
      GetObjectRequest req = new GetObjectRequest(bucketName, key);
      return amazonS3.getObject(req);
    } catch (Exception ex) {
      log.error("Error happened during aws s3object fetch", ex);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

  public URL getPreSignedUrl(String key, String bucketName) {
    try {
      GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, key,
          HttpMethod.GET);
      req.setExpiration(
          Date.from(Instant.now().plus(awsProperties.getPreSignExpire(), ChronoUnit.SECONDS)));
      return amazonS3.generatePresignedUrl(req);
    } catch (SdkClientException ex) {
      log.error("Error happened while pre-signing url for s3object", ex);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

}
