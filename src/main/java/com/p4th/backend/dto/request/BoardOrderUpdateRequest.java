package com.p4th.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "게시판 순서 변경 요청 DTO")
public class BoardOrderUpdateRequest {
    @Schema(description = "변경된 게시판 순서 (게시판 ID 리스트)", example = "[\"01HXYA4V8D5N1PZQF8KZBHY6XT\", \"01HXYA4V8D5N1PZQF8KZBHY7XT\", \"01HXZB4X9W7J2QKCG3MZPT8VF7\"]")
    private List<String> order;

    @Schema(description = "정렬 타입: 'normal' (수동 정렬, 기본값) 또는 'postCount' (게시글 수 기준 내림차순)", example = "normal")
    private String sortType;
}
