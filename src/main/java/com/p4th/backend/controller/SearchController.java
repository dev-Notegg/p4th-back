package com.p4th.backend.controller;

import com.p4th.backend.dto.response.search.SearchResponse;
import com.p4th.backend.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "검색 API", description = "검색 관련 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(
            summary = "검색",
            description = "작성자 닉네임, 게시글 내용, 제목을 포함한 게시글을 검색한다. " +
                    "boardId(게시판ID)를 제공하면 해당 게시판 내에서 검색하고, 제공하지 않으면 전체 게시판에서 검색한다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = SearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "검색어가 없거나 오류 발생",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<SearchResponse.SearchResult>> search(
            @Parameter(name = "boardId", description = "게시판 ID (옵션)")
            @RequestParam(value = "boardId", required = false) String boardId,
            @Parameter(name = "query", description = "검색어", required = true)
            @RequestParam("query") String query,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SearchResponse.SearchResult> response;
        response = searchService.search(boardId, query, pageable);
        return ResponseEntity.ok(response);
    }
}
