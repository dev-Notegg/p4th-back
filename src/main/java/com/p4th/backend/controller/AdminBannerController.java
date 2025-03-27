package com.p4th.backend.controller;

import com.p4th.backend.dto.response.ErrorResponse;
import com.p4th.backend.dto.request.BannerOrderUpdateRequest;
import com.p4th.backend.dto.response.admin.BannerCreationResponse;
import com.p4th.backend.dto.response.admin.BannerResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.AdminBannerService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "배너 관리 API", description = "배너 관리 관련 API")
@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final AdminBannerService adminBannerService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "배너 목록 조회", description = "배너 목록을 조회하며, 광고식별명으로 검색한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BannerResponse.class))),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<BannerResponse>> getBanners(
            @RequestParam(value = "search", required = false) String search,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BannerResponse> banners = adminBannerService.getBanners(search, pageable);
        return ResponseEntity.ok(banners);
    }

    @Operation(summary = "배너 등록", description = "새 배너를 등록한다. (각 필드는 @RequestParam으로 받고, 이미지 파일은 MultipartFile로 받음)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 등록 성공",
                    content = @Content(schema = @Schema(implementation = BannerCreationResponse.class))),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BannerCreationResponse> createBanner(
            @Parameter(name = "bannerName", description = "광고 식별명(배너 이름)", required = true)
            @RequestParam("bannerName") String bannerName,
            @Parameter(name = "linkUrl", description = "클릭 시 이동 링크 (옵션)")
            @RequestParam(value = "linkUrl", required = false) String linkUrl,
            @Parameter(name = "startDate", description = "광고 시작일 (yyyy-MM-dd)", required = true)
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(name = "endDate", description = "광고 종료일 (yyyy-MM-dd)", required = true)
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(name = "imageFile", description = "배너 이미지 파일", required = true,
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("imageFile") MultipartFile imageFile,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        String bannerId = adminBannerService.createBanner(userId, bannerName, linkUrl, startDate, endDate, imageFile);
        return ResponseEntity.ok(new BannerCreationResponse(bannerId));
    }

    @Operation(summary = "배너 삭제", description = "특정 배너를 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{bannerId}")
    public ResponseEntity<?> deleteBanner(
            @Parameter(name = "bannerId", description = "삭제할 배너 ID", required = true)
            @PathVariable("bannerId") String bannerId) {
        adminBannerService.deleteBanner(bannerId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "배너 순서 목록 조회",
            description = "광고기간 종료일이 지나지 않은 배너 순서 목록을 조회한다. 배너 노출순서 변경 목록을 조회할때 이용한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활성 배너 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BannerResponse.class))),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<List<BannerResponse>> getActiveBanners() {
        List<BannerResponse> banners = adminBannerService.getActiveBanners();
        return ResponseEntity.ok(banners);
    }

    @Operation(summary = "배너 순서 변경",
            description = "광고기간 종료일이 지나지 않은 배너의 노출 순서를 변경한다. (배너 ID 목록을 순서대로 전달)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배너 순서 변경 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/active/order")
    public ResponseEntity<?> updateActiveBannerOrder(
            @RequestBody BannerOrderUpdateRequest requestDto,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        adminBannerService.updateActiveBannerOrder(requestDto.getOrder(), userId);
        return ResponseEntity.ok().build();
    }
}
