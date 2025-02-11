package com.p4th.backend.controller;

import com.p4th.backend.dto.SearchResponse;
import com.p4th.backend.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "통합검색 API", description = "통합검색 관련 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "통합검색", description = "작성자 닉네임, 게시글 내용, 제목을 포함한 게시글을 검색하며, 카테고리/게시판별로 구분하여 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = SearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "검색어가 없거나 오류 발생",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @Parameter(name = "query", description = "검색어", required = true) @RequestParam("query") String query) {
        SearchResponse response = searchService.search(query);
        return ResponseEntity.ok().body(response);
    }
}
