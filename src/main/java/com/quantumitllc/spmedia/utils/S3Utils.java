package com.quantumitllc.spmedia.utils;

import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class S3Utils {

  public static String getUrlForPublicObject(@Nonnull String key, @Nonnull String bucket) {
    return "https://" + bucket + ".s3.amazonaws.com/" + key;
  }
}
