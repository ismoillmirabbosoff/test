package com.quantumitllc.spmedia.services;

import com.quantumitllc.spmedia.entity.FileTypes;
import com.quantumitllc.spmedia.entity.MediaMetaEntity;
import com.quantumitllc.spmedia.repo.MediaMetaRepository;
import com.quantumitllc.spmedia.serializers.mappers.MetaDataMapper;
import com.quantumitllc.spmedia.serializers.response.MetaData;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class MediaMetaService {

  private static final String ERROR_404 = "Resource not found";
  private final MediaMetaRepository mediaMetaRepository;
  private final MetaDataMapper metaDataMapper;

  MediaMetaEntity createSingleRecord(String extension, String originalName, String key,
      long size, boolean isPrivate, String userId) {
    var type = FileTypes.valueOf(extension.toUpperCase(Locale.ROOT));
    MediaMetaEntity mediaMetaEntity = new MediaMetaEntity(type, originalName, key, size, isPrivate);
    mediaMetaEntity.setUploadedBy(userId);

    return mediaMetaRepository.save(mediaMetaEntity);
  }

  public MetaData getRecord(String key) {
    return metaDataMapper.mapMediaMetaEntityToMetaData(getRecordByIdOrThrow(key));
  }

  void deleteRecord(MediaMetaEntity mediaMeta) {
    mediaMetaRepository.delete(mediaMeta);
  }

  void deleteManyRecords(List<String> keys) {
    mediaMetaRepository.deleteAllByKeyIn(keys);
  }

  List<MediaMetaEntity> findAll(List<String> keys) {
    return mediaMetaRepository.findAllByKeyIn(keys);
  }

  private MediaMetaEntity getRecordByIdOrThrow(String key) {
    var optRecord = mediaMetaRepository.findById(key);
    if (optRecord.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ERROR_404);
    } else {
      return optRecord.get();
    }
  }

  public void delete(String key) {
    mediaMetaRepository.deleteById(key);
  }
}
