package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "배너 노출 순서 변경 요청 DTO")
public class BannerOrderUpdateRequest {
    @Schema(description = "변경된 배너 ID 순서 목록", example = "[\"01JKQ90E6N6FP7YRWCVVC52KW0\", \"01JKQ90E6N6FP7YRWCVVC52KW1\"]")
    private List<String> order;
}
