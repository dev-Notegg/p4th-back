package com.p4th.backend.controller;

import com.p4th.backend.domain.Banner;
import com.p4th.backend.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "배너 API", description = "배너 관련 API")
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "배너 목록 조회",
               description = "관리자 페이지에서 등록한 배너 목록을 조회한다." +
                       "메인노출여부(display_yn)이 1이고 광고시작일~종료일이 현재 날짜에 해당되는 배너만 리턴되며 노출 순서(seq)대로 조회된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Banner.class))),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<Banner>> getBanners() {
        List<Banner> banners = bannerService.getBanners();
        return ResponseEntity.ok().body(banners);
    }
}
