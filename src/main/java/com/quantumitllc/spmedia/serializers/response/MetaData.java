package com.quantumitllc.spmedia.serializers.response;

import com.quantumitllc.spmedia.entity.FileTypes;
import java.time.Instant;
import lombok.Data;

@Data
public class MetaData {

  private String key;
  private FileTypes type;
  private String originalName;
  private String size;
  private boolean inUse;
  private boolean isPrivate;
  private Instant createdAt;
  private String uploadedBy;
}
