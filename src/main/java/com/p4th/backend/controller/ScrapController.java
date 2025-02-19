package com.p4th.backend.controller;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.Scrap;
import com.p4th.backend.dto.response.scrap.ScrapResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "게시글 스크랩 목록 조회", description = "사용자의 스크랩 목록을 조회한다. 폴더 ID를 제공하면 해당 폴더의 스크랩 목록을, 제공하지 않으면 전체 스크랩 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ScrapResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/users/scraps")
    public ResponseEntity<List<ScrapResponse>> getScraps(
            @RequestParam(value = "folderId", required = false) String folderId,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        List<Scrap> scraps = scrapService.getScraps(userId, folderId);
        List<ScrapResponse> responses = scraps.stream()
                .map(s -> ScrapResponse.from(s.getScrapId(), s.getPostId(), s.getScrapFolderId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "게시글 스크랩 삭제", description = "사용자의 스크랩을 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "게시글 스크랩 삭제 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping(value = "/users/scraps/{scrapId}")
    public ResponseEntity<?> deleteScrap(
            @Parameter(name = "scrapId", description = "스크랩 ID", required = true)
            @PathVariable("scrapId") String scrapId,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        String deletedScrapId = scrapService.deleteScrap(scrapId);
        return ResponseEntity.ok("{\"deleted\": \"" + deletedScrapId + "\"}");
    }

    @Operation(summary = "게시글 스크랩", description = "게시글을 스크랩한다. 폴더 ID를 제공하지 않으면 기본 폴더로 스크랩된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 성공",
                    content = @Content(schema = @Schema(implementation = ScrapResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "게시글 스크랩 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/posts/{postId}/scrap")
    public ResponseEntity<ScrapResponse> createScrap(
            @Parameter(name = "postId", description = "게시글 ID", required = true)
            @PathVariable("postId") String postId,
            @RequestBody(required = false) ScrapResponse requestBody,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        String scrapFolderId = requestBody != null ? requestBody.getScrapFolderId() : null;
        String scrapId = scrapService.createScrap(postId, scrapFolderId, userId);
        ScrapResponse response = ScrapResponse.from(scrapId, postId, scrapFolderId);
        return ResponseEntity.ok(response);
    }
}
