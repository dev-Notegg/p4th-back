package com.p4th.backend.controller;

import com.p4th.backend.dto.response.board.BoardResponse;
import com.p4th.backend.dto.response.board.PopularBoardResponse;
import com.p4th.backend.service.BoardService;
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
import java.util.List;

@Tag(name = "게시판 API", description = "게시판 관련 API")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @Operation(summary = "인기 게시판 목록 조회", description = "게시글 수를 기준으로 인기 게시판 상위 7개를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 게시판 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PopularBoardResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/popular")
    public ResponseEntity<List<PopularBoardResponse>> getPopularBoards() {
        List<PopularBoardResponse> popularBoards = boardService.getPopularBoards();
        return ResponseEntity.ok().body(popularBoards);
    }

    @Operation(summary = "특정 카테고리에 속한 게시판 목록 조회", description = "특정 카테고리 ID를 입력받아 해당 카테고리 정보와 게시판 목록을 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 내 게시판 조회 성공",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류 또는 기타 문제",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<List<BoardResponse>> getBoardsByCategory(
            @Parameter(name = "categoryId", description = "카테고리 ID", required = true)
            @PathVariable String categoryId) {
        List<BoardResponse> boards = boardService.getBoardsByCategory(categoryId);
        return ResponseEntity.ok().body(boards);
    }
}
