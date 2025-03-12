package com.p4th.backend.controller;

import com.p4th.backend.domain.Banner;
import com.p4th.backend.dto.response.board.PopularBoardResponse;
import com.p4th.backend.dto.response.post.PopularPostResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.MainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "메인 API", description = "메인 관련 API(배너, 인기게시글 등)")
@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "배너 목록 조회",
               description = "관리자 페이지에서 등록한 배너 목록을 조회한다." +
                       "\n 메인노출여부(display_yn)이 1이고 광고시작일~종료일이 현재 날짜에 해당되는 배너만 리턴되며 노출 순서(seq)대로 조회된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Banner.class))),
            @ApiResponse(responseCode = "500", description = "배너 목록 조회 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/banners")
    public ResponseEntity<List<Banner>> getBanners() {
        List<Banner> banners = mainService.getBanners();
        return ResponseEntity.ok().body(banners);
    }

    @Operation(summary = "인기 게시판 목록 조회", description = "게시글 수를 기준으로 인기 게시판 상위 7개를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 게시판 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PopularBoardResponse.class))),
            @ApiResponse(responseCode = "500", description = "인기 게시판 목록 조회 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/popular/boards")
    public ResponseEntity<List<PopularBoardResponse>> getPopularBoards() {
        List<PopularBoardResponse> popularBoards = mainService.getPopularBoards();
        return ResponseEntity.ok().body(popularBoards);
    }


    @Operation(summary = "인기 게시글 목록 조회", description = "인기 게시글 목록(최대 20개)을 반환한다. period 파라미터(HOURLY, DAILY, WEEKLY, MONTHLY)를 통해 조회 기간을 지정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PopularPostResponse.class))),
            @ApiResponse(responseCode = "500", description = "인기 게시글 조회 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/popular/posts")
    public ResponseEntity<List<?>> getPopularPosts(
            @Parameter(name = "period", description = "조회 기간 (HOURLY, DAILY, WEEKLY, MONTHLY)", example = "DAILY")
            @RequestParam(value = "period", defaultValue = "DAILY") String period,
            HttpServletRequest httpRequest) {
        String userId = jwtProvider.resolveUserId(httpRequest);
        List<?> popularPosts = mainService.getPopularPosts(period, userId);
        return ResponseEntity.ok().body(popularPosts);
    }
}
