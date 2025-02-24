package com.p4th.backend.dto.response.block;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "차단 응답 DTO")
public class BlockResponse {
    @Schema(description = "차단 ID", example = "01JKQ90E6N6FP7YRWCVVC52KW0")
    private String blockId;
}
