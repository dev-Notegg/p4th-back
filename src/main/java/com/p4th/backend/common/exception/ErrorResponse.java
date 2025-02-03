package com.p4th.backend.common.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private int errorCode;         // 예) 10201
    private String errorMessage;   // 예) "PHONE_DUPLICATED"
    private String status;         // 예) "CONFLICT"
}
