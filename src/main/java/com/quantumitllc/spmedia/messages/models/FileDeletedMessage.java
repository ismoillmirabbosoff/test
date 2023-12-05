package com.quantumitllc.spmedia.messages.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class FileDeletedMessage implements Serializable {

  @JsonProperty("isPermanent")
  private boolean isPermanent;

  @JsonProperty("files")
  private List<String> files = new ArrayList<>();

}

