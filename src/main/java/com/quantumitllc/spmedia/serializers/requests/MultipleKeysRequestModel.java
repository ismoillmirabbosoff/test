package com.quantumitllc.spmedia.serializers.requests;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MultipleKeysRequestModel {

  private boolean ignorePartialErrors = true;
  @Size(min = 1, max = 30, message = "keys field must contain at least 1 and at most 30 object keys")
  private List<String> keys;
}
