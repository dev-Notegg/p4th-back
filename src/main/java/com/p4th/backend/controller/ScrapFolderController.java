package com.p4th.backend.controller;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.ScrapFolder;
import com.p4th.backend.dto.response.scrap.ScrapFolderResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.ScrapFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/scrap-folders")
@RequiredArgsConstructor
public class ScrapFolderController {

    private final ScrapFolderService scrapFolderService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "스크랩 폴더 목록 조회", description = "사용자의 스크랩 폴더 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ScrapFolderResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ScrapFolderResponse>> getScrapFolders(HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        List<ScrapFolder> folders = scrapFolderService.getScrapFolders(userId);
        List<ScrapFolderResponse> responses = folders.stream()
                .map(ScrapFolderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "스크랩 폴더 생성", description = "새 스크랩 폴더를 생성한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 생성 성공",
                    content = @Content(schema = @Schema(implementation = ScrapFolderResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 폴더 생성 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ScrapFolderResponse> createScrapFolder(
            @RequestBody ScrapFolderResponse requestBody,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        ScrapFolder folder = scrapFolderService.createScrapFolder(userId, requestBody.getFolderName());
        return ResponseEntity.ok(ScrapFolderResponse.from(folder));
    }

    @Operation(summary = "스크랩 폴더명 변경", description = "스크랩 폴더의 이름을 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더명 변경 성공",
                    content = @Content(schema = @Schema(implementation = ScrapFolderResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 폴더명 변경 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/{scrapFolderId}")
    public ResponseEntity<ScrapFolderResponse> updateScrapFolderName(
            @PathVariable("scrapFolderId") String scrapFolderId,
            @RequestBody ScrapFolderResponse requestBody,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        ScrapFolder folder = scrapFolderService.updateScrapFolderName(scrapFolderId, requestBody.getFolderName(), userId);
        return ResponseEntity.ok(ScrapFolderResponse.from(folder));
    }

    @Operation(summary = "스크랩 폴더 순서 변경", description = "스크랩 폴더의 순서를 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 순서 변경 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 폴더 순서 변경 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/order")
    public ResponseEntity<?> updateScrapFolderOrder(
            @RequestBody List<String> order,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        boolean updated = scrapFolderService.updateScrapFolderOrder(order, userId);
        return ResponseEntity.ok("{\"updated\": " + updated + "}");
    }

    @Operation(summary = "스크랩 폴더 삭제", description = "스크랩 폴더를 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 폴더 삭제 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping(value = "/{scrapFolderId}")
    public ResponseEntity<?> deleteScrapFolder(
            @PathVariable("scrapFolderId") String scrapFolderId,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        boolean deleted = scrapFolderService.deleteScrapFolder(scrapFolderId, userId);
        return ResponseEntity.ok("{\"deleted\": " + deleted + "}");
    }
}
