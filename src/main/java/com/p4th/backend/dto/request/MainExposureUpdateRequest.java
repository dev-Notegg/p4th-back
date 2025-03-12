package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "메인 노출 설정 변경 요청 DTO")
public class MainExposureUpdateRequest {
    @Schema(description = "메인 노출 여부 (1=노출, 0=숨김)", example = "1")
    private int mainExposure;
}
