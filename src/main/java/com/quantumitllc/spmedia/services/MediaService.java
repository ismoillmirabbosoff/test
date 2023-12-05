package com.quantumitllc.spmedia.services;

import com.amazonaws.services.s3.model.S3Object;
import com.google.common.io.Files;
import com.quantumitllc.spmedia.config.AwsProperties;
import com.quantumitllc.spmedia.entity.FileTypes;
import com.quantumitllc.spmedia.entity.MediaMetaEntity;
import com.quantumitllc.spmedia.messages.models.FileDeletedMessage;
import com.quantumitllc.spmedia.messages.producers.EventProducer;
import com.quantumitllc.spmedia.serializers.mappers.MetaDataMapper;
import com.quantumitllc.spmedia.serializers.response.MetaData;
import com.quantumitllc.spmedia.serializers.response.PreSignedUrlResponse;
import com.quantumitllc.spmedia.utils.S3Utils;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
@Service
public class MediaService {

  private static final String UNTITLED = "untitled";

  private final MediaMetaService mediaMetaService;
  private final CustomS3Service s3Service;
  private final EventProducer eventProducer;
  private final AwsProperties properties;
  private final MetaDataMapper metaDataMapper;


  public PreSignedUrlResponse uploadFile(MultipartFile file, boolean isPrivate, boolean meta,
      String folder, String userId) {

    String bucketName = getBucketName(isPrivate);
    String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse(UNTITLED);
    String extension = Files.getFileExtension(originalName);
    long sizeInBytes = file.getSize();

    try {
      FileTypes.valueOf(extension.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      log.error("File with invalid file extension", ex);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file extension ["
          + extension
          + "], Allowed extensions: "
          + Arrays.toString(FileTypes.values()), ex);
    }

    String key = s3Service.uploadGivenFile(file, bucketName, folder);
    var entity = mediaMetaService.createSingleRecord(extension, originalName, key,
        sizeInBytes, isPrivate, userId);

    PreSignedUrlResponse response;
    try {
      response = getFileUrl(key);
    } catch (Exception ex) {
      response = new PreSignedUrlResponse();
      response.setKey(key);
    }

    if(meta){
      response.setMeta(metaDataMapper.mapMediaMetaEntityToMetaData(entity));
    }

    return response;
  }

  public void removeFile(String key) {
    MetaData metaData = mediaMetaService.getRecord(key);
    String bucketName = getBucketName(metaData.isPrivate());
    s3Service.removeObject(key, bucketName);
    mediaMetaService.delete(key);
    try {
      FileDeletedMessage fileDeletedMessage = new FileDeletedMessage(true, List.of(key));
      eventProducer.sendFanout(fileDeletedMessage);
    } catch (Exception ex) {
      log.error("Could not send file deletion event", ex);
    }
  }

  public int removeMultipleFiles(List<String> keys, boolean ignorePartialErrors) {
    var records = mediaMetaService.findAll(keys);
    if (records.size() > 0) {
      if (records.size() != keys.size() && !ignorePartialErrors) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some of the keys are invalid");
      }
      var privateRecords = records.stream().filter(MediaMetaEntity::isPrivate)
          .map(MediaMetaEntity::getKey).collect(Collectors.toList());
      var publicRecords = records.stream().filter(record -> !record.isPrivate())
          .map(MediaMetaEntity::getKey).collect(Collectors.toList());

      mediaMetaService.deleteManyRecords(keys);
      s3Service.removeMultipleObjects(privateRecords, properties.getPrivateBucket());
      s3Service.removeMultipleObjects(publicRecords, properties.getPublicBucket());

      try {
        var deletedKeys = records.stream().map(MediaMetaEntity::getKey)
            .collect(Collectors.toList());
        FileDeletedMessage message = new FileDeletedMessage(true, deletedKeys);
        eventProducer.sendFanout(message);
      } catch (Exception ex) {
        log.error("Could not send file deletion event", ex);
      }
      return records.size();
    }

    return 0;
  }

  public S3Object retrieveFile(String key) {
    MetaData metaData = mediaMetaService.getRecord(key);
    String bucketName = getBucketName(metaData.isPrivate());
    return s3Service.fetchObject(key, bucketName);
  }

  public PreSignedUrlResponse getFileUrl(String key) {
    MetaData metaData = mediaMetaService.getRecord(key);
    String bucketName = getBucketName(metaData.isPrivate());
    var ps = new PreSignedUrlResponse(key, null, bucketName);
    setPreSignUrl(ps);
    return ps;
  }

  public List<PreSignedUrlResponse> getUrlsForMultipleFiles(List<String> keys,
      boolean ignorePartialErrors) {
    var records = mediaMetaService.findAll(keys);
    List<PreSignedUrlResponse> result = records.stream()
        .map(record -> new PreSignedUrlResponse(record.getKey(), null,
            getBucketName(record.isPrivate())))
        .collect(Collectors.toList());
    for (PreSignedUrlResponse presignedUrlResponse : result) {
      try {
        setPreSignUrl(presignedUrlResponse);
      } catch (Exception ex) {
        if (!ignorePartialErrors) {
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      }
    }
    return result;
  }

  private void setPreSignUrl(PreSignedUrlResponse ps) {
    if (Objects.equals(ps.getBucketName(), properties.getPublicBucket())) {
      String url = S3Utils.getUrlForPublicObject(ps.getKey(), properties.getPublicBucket());
      ps.setPreSignedUrl(url);
    } else if (Objects.equals(ps.getBucketName(), properties.getPrivateBucket())) {
      URL url = s3Service.getPreSignedUrl(ps.getKey(), properties.getPrivateBucket());
      ps.setPreSignedUrl(url.toExternalForm());
    }
  }

  private String getBucketName(boolean isPrivate) {
    return isPrivate ? properties.getPrivateBucket() : properties.getPublicBucket();
  }
}
