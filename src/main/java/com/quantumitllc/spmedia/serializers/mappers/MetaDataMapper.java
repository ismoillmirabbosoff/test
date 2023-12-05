package com.quantumitllc.spmedia.serializers.mappers;

import com.quantumitllc.spmedia.entity.MediaMetaEntity;
import com.quantumitllc.spmedia.serializers.response.MetaData;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import org.springframework.stereotype.Component;

@Component
public class MetaDataMapper {

  public MetaData mapMediaMetaEntityToMetaData(MediaMetaEntity entity) {
    MetaData metaData = new MetaData();

    metaData.setKey(entity.getKey());
    metaData.setOriginalName(entity.getOriginalName());
    metaData.setCreatedAt(entity.getCreatedAt());
    metaData.setType(entity.getType());
    metaData.setInUse(entity.isInUse());
    metaData.setPrivate(entity.isPrivate());
    metaData.setUploadedBy(entity.getUploadedBy());
    metaData.setSize(humanReadableByteCountBin(entity.getSize()));

    return metaData;
  }

  public static String humanReadableByteCountBin(long bytes) {
    long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
    if (absB < 1024) {
      return bytes + " B";
    }
    long value = absB;
    CharacterIterator ci = new StringCharacterIterator("KMGTPE");
    for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
      value >>= 10;
      ci.next();
    }
    value *= Long.signum(bytes);
    return String.format("%.1f %cB", value / 1024.0, ci.current());
  }

}
