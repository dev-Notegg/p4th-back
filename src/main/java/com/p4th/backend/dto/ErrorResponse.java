package com.p4th.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private int errorCode;
    private String errorMessage;
    private String status;
    private LocalDateTime timestamp;
}
