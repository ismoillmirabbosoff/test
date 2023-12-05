package com.quantumitllc.spmedia.entity;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "media_metas")
public class MediaMetaEntity {

  @Id
  private String key;
  private FileTypes type;
  private String originalName;
  private long size;
  private boolean inUse;
  private boolean isPrivate;
  private Instant createdAt;
  private String uploadedBy;

  public MediaMetaEntity(FileTypes type, String originalName, String key, long size,
      boolean isPrivate) {
    this.type = type;
    this.originalName = originalName;
    this.key = key;
    this.size = size;
    this.isPrivate = isPrivate;
    this.createdAt = Instant.now();
    this.inUse = true;
  }

}
