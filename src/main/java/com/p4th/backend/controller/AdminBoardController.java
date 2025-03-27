package com.p4th.backend.controller;

import com.p4th.backend.dto.response.ErrorResponse;
import com.p4th.backend.dto.request.BoardCreationRequest;
import com.p4th.backend.dto.request.BoardUpdateRequest;
import com.p4th.backend.dto.response.admin.BoardCreationResponse;
import com.p4th.backend.dto.response.admin.BoardDeletionInfoResponse;
import com.p4th.backend.dto.response.admin.BoardResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.AdminBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시판 관리 API", description = "게시판 관리 관련 API")
@RestController
@RequestMapping("/api/admin/boards")
@RequiredArgsConstructor
public class AdminBoardController {

    private final AdminBoardService adminBoardService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "게시판 목록 조회", description = "게시판 목록을 조회하며, 게시판 ID, 게시판명, 카테고리명으로 검색 가능하다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<BoardResponse>> getBoards(
            @Parameter(name = "boardId", description = "검색할 게시판 ID (옵션)")
            @RequestParam(value = "boardId", required = false) String boardId,
            @Parameter(name = "boardName", description = "검색할 게시판명 (옵션)")
            @RequestParam(value = "boardName", required = false) String boardName,
            @Parameter(name = "categoryName", description = "검색할 카테고리명 (옵션)")
            @RequestParam(value = "categoryName", required = false) String categoryName,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardResponse> boards = adminBoardService.getBoards(boardId, boardName, categoryName, pageable);
        return ResponseEntity.ok(boards);
    }

    @Operation(summary = "게시판 추가", description = "새 게시판을 추가한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "해당 카테고리 내에 이미 동일한 게시판명 존재",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<BoardCreationResponse> createBoard(
            @RequestBody BoardCreationRequest requestDto,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        String boardId = adminBoardService.createBoard(userId, requestDto.getBoardName(), requestDto.getCategoryId(), requestDto.getBoardLevel());
        return ResponseEntity.ok(new BoardCreationResponse(boardId));
    }

    @Operation(summary = "게시판 수정", description = "특정 게시판의 정보를 수정한다. (게시판명, 카테고리, 게시판 레벨 변경)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 게시판을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "해당 카테고리 내에 이미 동일한 게시판명 존재",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(
            @Parameter(name = "boardId", description = "수정할 게시판 ID", required = true)
            @PathVariable("boardId") String boardId,
            @RequestBody BoardUpdateRequest requestDto,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        adminBoardService.updateBoard(userId, boardId, requestDto.getBoardName(), requestDto.getCategoryId(), requestDto.getBoardLevel());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시판 삭제 전 정보 조회", description = "삭제 전 확인을 위해 해당 게시판의 이름, 게시글 수, 댓글 수를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 게시판을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{boardId}/deletion-info")
    public ResponseEntity<BoardDeletionInfoResponse> getBoardDeletionInfo(
            @Parameter(name = "boardId", description = "게시판 ID", required = true)
            @PathVariable("boardId") String boardId) {
        BoardDeletionInfoResponse info = adminBoardService.getBoardDeletionInfo(boardId);
        return ResponseEntity.ok(info);
    }

    @Operation(summary = "게시판 삭제", description = "확인 후 해당 게시판을 삭제한다. (삭제 후 복구 불가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 게시판을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(
            @Parameter(name = "boardId", description = "삭제할 게시판 ID", required = true)
            @PathVariable("boardId") String boardId) {
        adminBoardService.deleteBoard(boardId);
        return ResponseEntity.ok().build();
    }
}
