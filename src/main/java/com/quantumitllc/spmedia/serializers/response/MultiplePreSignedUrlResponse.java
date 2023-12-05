package com.quantumitllc.spmedia.serializers.response;

import java.util.List;

public record MultiplePreSignedUrlResponse(List<PreSignedUrlResponse> content) {

}
