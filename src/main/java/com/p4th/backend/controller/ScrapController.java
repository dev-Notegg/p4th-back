package com.p4th.backend.controller;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.dto.request.ScrapFolderCreateRequest;
import com.p4th.backend.dto.request.ScrapFolderOrderUpdateRequest;
import com.p4th.backend.dto.request.ScrapFolderUpdateRequest;
import com.p4th.backend.dto.response.scrap.ScrapCreateResponse;
import com.p4th.backend.dto.response.scrap.ScrapFolderResponse;
import com.p4th.backend.dto.response.scrap.ScrapResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "스크랩 API", description = "스크랩 폴더 및 게시글 스크랩 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScrapController {

    private final ScrapService scrapService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "스크랩 폴더 목록 조회", description = "사용자의 스크랩 폴더 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 목록 조회 성공")
    })
    @GetMapping(value = "/users/scrap-folders")
    public ResponseEntity<List<ScrapFolderResponse>> getScrapFolders(HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        List<ScrapFolderResponse> folders = scrapService.getScrapFolders(userId).stream()
                .map(folder -> {
                    ScrapFolderResponse resp = new ScrapFolderResponse();
                    resp.setScrapFolderId(folder.getScrapFolderId());
                    resp.setFolderName(folder.getFolderName());
                    resp.setSortOrder(folder.getSortOrder());
                    resp.setCreatedAt(folder.getCreatedAt());
                    return resp;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(folders);
    }

    @Operation(summary = "스크랩 폴더 생성", description = "새 스크랩 폴더를 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 생성 성공")
    })
    @PostMapping(value = "/users/scrap-folders")
    public ResponseEntity<ScrapFolderResponse> createScrapFolder(
            @Valid @RequestBody ScrapFolderCreateRequest request,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        var folder = scrapService.createScrapFolder(userId, request.getFolderName());
        ScrapFolderResponse response = new ScrapFolderResponse();
        response.setScrapFolderId(folder.getScrapFolderId());
        response.setFolderName(folder.getFolderName());
        response.setSortOrder(folder.getSortOrder());
        response.setCreatedAt(folder.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스크랩 폴더명 변경", description = "지정한 스크랩 폴더의 이름을 변경한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스크랩 폴더명 변경 성공")
    })
    @PutMapping(value = "/users/scrap-folders/{folderId}")
    public ResponseEntity<ScrapFolderResponse> updateScrapFolderName(
            @Parameter(name = "folderId", description = "스크랩 폴더 ID", required = true)
            @PathVariable("folderId") String folderId,
            @Valid @RequestBody ScrapFolderUpdateRequest request,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        var updatedFolder = scrapService.updateScrapFolderName(folderId, request.getFolderName(), userId);
        ScrapFolderResponse response = new ScrapFolderResponse();
        response.setScrapFolderId(updatedFolder.getScrapFolderId());
        response.setFolderName(updatedFolder.getFolderName());
        response.setSortOrder(updatedFolder.getSortOrder());
        response.setCreatedAt(updatedFolder.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스크랩 폴더 순서 변경", description = "스크랩 폴더의 순서를 변경한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 순서 변경 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class)))
    })
    @PutMapping(value = "/users/scrap-folders/order")
    public ResponseEntity<?> updateScrapFolderOrder(
            @Valid @RequestBody ScrapFolderOrderUpdateRequest request,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        boolean updated = scrapService.updateScrapFolderOrder(request.getOrder(), userId);
        return ResponseEntity.ok("{\"updated\": " + updated + "}");
    }

    @Operation(summary = "스크랩 폴더 삭제", description = "지정한 스크랩 폴더를 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class)))
    })
    @DeleteMapping(value = "/users/scrap-folders/{folderId}")
    public ResponseEntity<?> deleteScrapFolder(
            @Parameter(name = "folderId", description = "스크랩 폴더 ID", required = true)
            @PathVariable("folderId") String folderId,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        boolean deleted = scrapService.deleteScrapFolder(folderId);
        return ResponseEntity.ok("{\"deleted\": " + deleted + "}");
    }

    @Operation(summary = "게시글 스크랩 목록 조회", description = "사용자의 게시글 스크랩 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/users/scraps")
    public ResponseEntity<List<ScrapResponse>> getScraps(HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        List<ScrapResponse> scraps = scrapService.getScraps(userId).stream()
                .map(scrap -> {
                    ScrapResponse resp = new ScrapResponse();
                    resp.setScrapId(scrap.getScrapId());
                    resp.setPostId(scrap.getPostId());
                    resp.setScrappedAt(scrap.getScrappedAt());
                    return resp;
                })
                .toList();
        return ResponseEntity.ok(scraps);
    }

    @Operation(summary = "게시글 스크랩 삭제", description = "사용자의 게시글 스크랩을 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping(value = "/users/scraps/{scrapId}")
    public ResponseEntity<?> deleteScrap(
            @Parameter(name = "scrapId", description = "게시글 스크랩 ID", required = true)
            @PathVariable("scrapId") String scrapId,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        boolean deleted = scrapService.deleteScrap(scrapId);
        return ResponseEntity.ok("{\"deleted\": " + deleted + "}");
    }

    @Operation(summary = "게시글 스크랩", description = "게시글을 스크랩한다. (선택적으로 스크랩 폴더 ID를 전달할 수 있다.)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 스크랩 성공",
                    content = @Content(schema = @Schema(implementation = ScrapCreateResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/posts/{postId}/scrap")
    public ResponseEntity<ScrapCreateResponse> createScrap(
            @Parameter(name = "postId", description = "게시글 ID", required = true)
            @PathVariable("postId") String postId,
            @RequestBody(required = false) String scrapFolderId,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        ScrapCreateResponse response = new ScrapCreateResponse(scrapService.createScrap(postId, scrapFolderId).getScrapId());
        return ResponseEntity.ok(response);
    }
}
