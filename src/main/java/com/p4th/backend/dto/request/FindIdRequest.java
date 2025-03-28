package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FindIdRequest {
    @Schema(description = "가입 시 받은 패쓰코드")
    private String passCode;
}
