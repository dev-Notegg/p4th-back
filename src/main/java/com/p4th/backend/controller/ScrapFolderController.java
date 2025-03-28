package com.p4th.backend.controller;

import com.p4th.backend.annotation.RequireLogin;
import com.p4th.backend.dto.response.ErrorResponse;
import com.p4th.backend.domain.ScrapFolder;
import com.p4th.backend.dto.request.ScrapFolderNameRequest;
import com.p4th.backend.dto.request.ScrapFolderOrderUpdateRequest;
import com.p4th.backend.dto.response.scrap.ScrapFolderResponse;
import com.p4th.backend.service.ScrapFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "스크랩폴더 API", description = "스크랩폴더 관련 API")
@RestController
@RequestMapping("/api/users/scrap-folders")
@RequiredArgsConstructor
public class ScrapFolderController {

    private final ScrapFolderService scrapFolderService;

    @Operation(summary = "스크랩 폴더 목록 조회", description = "사용자의 스크랩 폴더 목록을 조회한다. 각 폴더에는 해당 폴더에 속한 스크랩 게시글 개수가 포함된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ScrapFolderResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ScrapFolderResponse>> getScrapFolders(HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("currentUserId");
        List<ScrapFolderResponse> responses = scrapFolderService.getScrapFolders(currentUserId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "스크랩 폴더 생성", description = "새 스크랩 폴더를 생성한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 생성 성공",
                    content = @Content(schema = @Schema(implementation = ScrapFolderResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 폴더 생성 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @PostMapping
    public ResponseEntity<ScrapFolderResponse> createScrapFolder(
            @RequestBody ScrapFolderNameRequest requestBody,
            HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("currentUserId");
        ScrapFolder folder = scrapFolderService.createScrapFolder(currentUserId, requestBody.getFolderName());
        return ResponseEntity.ok(ScrapFolderResponse.from(folder));
    }

    @Operation(summary = "스크랩 폴더명 변경", description = "스크랩 폴더의 이름을 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더명 변경 성공",
                    content = @Content(schema = @Schema(implementation = ScrapFolderResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 스크랩 폴더가 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 폴더명 변경 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @PutMapping(value = "/{scrapFolderId}")
    public ResponseEntity<?> updateScrapFolderName(
            @PathVariable("scrapFolderId") String scrapFolderId,
            @RequestBody ScrapFolderNameRequest requestBody,
            HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("currentUserId");
        boolean updated = scrapFolderService.updateScrapFolderName(scrapFolderId, requestBody.getFolderName(), currentUserId);
        return ResponseEntity.ok("{\"updated\": " + updated + "}");
    }

    @Operation(summary = "스크랩 폴더 순서 변경", description = "스크랩 폴더의 순서를 변경한다. 스크랩 폴더ID를 배열로 순서대로 보내준다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 순서 변경 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류: 모든 스크랩 폴더의 순서를 지정하지 않았거나, 요청된 폴더 목록이 올바르지 않은 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 폴더 순서 변경 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @PutMapping(value = "/order")
    public ResponseEntity<?> updateScrapFolderOrder(
            @RequestBody ScrapFolderOrderUpdateRequest requestDto,
            HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("currentUserId");
        boolean updated = scrapFolderService.updateScrapFolderOrder(requestDto.getOrder(), currentUserId);
        return ResponseEntity.ok("{\"updated\": " + updated + "}");
    }

    @Operation(summary = "스크랩 폴더 삭제", description = "스크랩 폴더를 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩 폴더 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "401", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 스크랩 폴더가 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 폴더 삭제 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequireLogin
    @DeleteMapping(value = "/{scrapFolderId}")
    public ResponseEntity<?> deleteScrapFolder(
            @PathVariable("scrapFolderId") String scrapFolderId,
            HttpServletRequest request) {
        String currentUserId = (String) request.getAttribute("currentUserId");
        boolean deleted = scrapFolderService.deleteScrapFolder(scrapFolderId, currentUserId);
        return ResponseEntity.ok("{\"deleted\": " + deleted + "}");
    }
}
