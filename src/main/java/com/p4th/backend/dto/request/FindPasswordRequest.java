package com.p4th.backend.dto.request;

import lombok.Data;

@Data
public class FindPasswordRequest {
    private String userId;
    private String passCode;
}
