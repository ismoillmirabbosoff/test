package com.quantumitllc.spmedia.repo;


import com.quantumitllc.spmedia.entity.MediaMetaEntity;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MediaMetaRepository extends MongoRepository<MediaMetaEntity, String> {

  List<MediaMetaEntity> findAllByKeyIn(List<String> keys);

  void deleteAllByKeyIn(List<String> keys);
}
