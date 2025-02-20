package com.p4th.backend.controller;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.Scrap;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.dto.response.post.PostListResponse;
import com.p4th.backend.dto.response.scrap.ScrapResponse;
import com.p4th.backend.dto.response.scrap.UserScrapResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "스크랩 API", description = "게시글 스크랩 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;
    private final JwtProvider jwtProvider;
    private final PostMapper postMapper;

    @Operation(summary = "게시글 스크랩 목록 조회", description = "사용자의 스크랩 목록을 조회한다. 폴더 ID를 제공하면 해당 폴더의 스크랩 목록을, 제공하지 않으면 전체 스크랩 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ScrapResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/users/scraps")
    public ResponseEntity<UserScrapResponse> getScraps(
            @Parameter(name = "scrapFolderId", description = "스크랩 폴더 ID (옵션)")
            @RequestParam(value = "scrapFolderId", required = false) String scrapFolderId,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        List<Scrap> scraps = scrapService.getScraps(userId, scrapFolderId);
        List<PostListResponse> content = scraps.stream()
                .map(scrap -> {
                    Post post = postMapper.getPostDetail(scrap.getPostId());
                    return PostListResponse.from(post);
                })
                .collect(Collectors.toList());
        UserScrapResponse response = new UserScrapResponse();
        response.setScrapFolderId(scrapFolderId);
        response.setContent(content);
        return ResponseEntity.ok(response);
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
        String deletedScrapId = scrapService.deleteScrap(scrapId, userId);
        return ResponseEntity.ok("{\"deleted\": \"" + deletedScrapId + "\"}");
    }

    @Operation(summary = "게시글 스크랩", description = "게시글을 스크랩한다. 폴더 ID를 제공하지 않으면 폴더 ID없는 전체로 스크랩된다.")
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
            @Parameter(name = "scrapFolderId", description = "스크랩 폴더 ID (옵션)")
            @RequestParam(value = "scrapFolderId", required = false) String scrapFolderId,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        String scrapId = scrapService.createScrap(postId, scrapFolderId, userId);
        ScrapResponse response = ScrapResponse.from(scrapId, postId, scrapFolderId);
        return ResponseEntity.ok(response);
    }
}
