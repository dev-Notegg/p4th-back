package com.p4th.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private int errorCode;
    private String errorMessage;
    private String status;
    private LocalDateTime timestamp;

    private ErrorResponse(Builder builder) {
        this.errorCode = builder.errorCode;
        this.errorMessage = builder.errorMessage;
        this.status = builder.status;
        this.timestamp = builder.timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int errorCode;
        private String errorMessage;
        private String status;
        private LocalDateTime timestamp;

        public Builder errorCode(int errorCode) { this.errorCode = errorCode; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public ErrorResponse build() { return new ErrorResponse(this); }
    }
}
