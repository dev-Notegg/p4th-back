package com.p4th.backend.controller;

import com.p4th.backend.annotation.RequireLogin;
import com.p4th.backend.dto.response.ErrorResponse;
import com.p4th.backend.dto.response.scrap.ScrapPostListResponse;
import com.p4th.backend.dto.response.scrap.ScrapResponse;
import com.p4th.backend.service.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "스크랩 API", description = "게시글 스크랩 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    @Operation(
            summary = "게시글 스크랩 목록 조회",
            description = "사용자의 스크랩 게시글 목록을 조회한다." +
                    "\n 스크랩 폴더 ID를 제공하면 해당 폴더의 스크랩 목록을, 제공하지 않으면 전체 스크랩 목록을 조회한다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ScrapPostListResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 게시글 목록 조회 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @GetMapping(value = "/users/scraps")
    public ResponseEntity<Page<ScrapPostListResponse>> getScrapPosts(
            @Parameter(name = "scrapFolderId", description = "스크랩 폴더 ID (옵션)")
            @RequestParam(value = "scrapFolderId", required = false) String scrapFolderId,
            @Parameter(hidden = true) @PageableDefault(sort = "scrappedAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("currentUserId");
        Page<ScrapPostListResponse> response = scrapService.getScrapPosts(currentUserId, scrapFolderId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 스크랩 삭제", description = "사용자의 스크랩을 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "스크랩을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "게시글 스크랩 삭제 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @DeleteMapping(value = "/users/scraps/{scrapId}")
    public ResponseEntity<?> deleteScrap(
            @Parameter(name = "scrapId", description = "스크랩 ID", required = true)
            @PathVariable("scrapId") String scrapId,
            HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("currentUserId");
        String deletedScrapId = scrapService.deleteScrap(scrapId, currentUserId);
        return ResponseEntity.ok("{\"deleted\": \"" + deletedScrapId + "\"}");
    }

    @Operation(summary = "게시글 스크랩", description = "게시글을 스크랩한다. 폴더 ID를 제공하지 않으면 폴더 ID없는 전체로 스크랩된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 성공",
                    content = @Content(schema = @Schema(implementation = ScrapResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "스크랩 폴더를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "게시글 스크랩 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @PostMapping(value = "/posts/{postId}/scrap")
    public ResponseEntity<ScrapResponse> createScrap(
            @Parameter(name = "postId", description = "게시글 ID", required = true)
            @PathVariable("postId") String postId,
            @Parameter(name = "scrapFolderId", description = "스크랩 폴더 ID (옵션)")
            @RequestParam(value = "scrapFolderId", required = false) String scrapFolderId,
            HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("currentUserId");
        String scrapId = scrapService.createScrap(postId, scrapFolderId, currentUserId);
        ScrapResponse response = ScrapResponse.from(scrapId, postId, scrapFolderId);
        return ResponseEntity.ok(response);
    }
}
