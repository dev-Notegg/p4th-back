package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "차단 요청 DTO")
public class BlockRequest {
    @Schema(description = "차단 여부 (true: 차단, false: 차단 해제)", example = "true")
    private boolean blocked;
}
