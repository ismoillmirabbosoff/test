package com.quantumitllc.spmedia.serializers.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PreSignedUrlResponse {

  private String key;
  private String preSignedUrl;
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String bucketName;
  @JsonInclude(Include.NON_NULL)
  private MetaData meta;

  public PreSignedUrlResponse(String key, String preSignedUrl, String bucketName) {
    this.key = key;
    this.preSignedUrl = preSignedUrl;
    this.bucketName = bucketName;
  }
}
