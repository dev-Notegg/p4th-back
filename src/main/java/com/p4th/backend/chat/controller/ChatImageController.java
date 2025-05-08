package com.p4th.backend.chat.controller;

import com.p4th.backend.chat.upload.ChatImageUploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/chat/images")
@RequiredArgsConstructor
public class ChatImageController {

    private final ChatImageUploader chatImageUploader;

    @Operation(
        summary = "채팅 이미지 업로드",
        description = "이미지 파일을 업로드한 뒤 접근 가능한 CDN URL을 반환한다.",
        requestBody = @RequestBody(
            description = "업로드할 이미지 파일 (jpg, png, gif, 최대 5MB)",
            required = true,
            content = @Content(mediaType = "multipart/form-data",
                schema = @Schema(type = "object", implementation = UploadRequest.class))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "업로드 성공 (CDN 이미지 URL 반환)")
        }
    )
    @PostMapping
    public ResponseEntity<Map<String, String>> uploadChatImage(@RequestPart MultipartFile file,
                                                               @RequestParam String roomId) {
        String imageUrl = chatImageUploader.upload(file, roomId);
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }

    @Schema(name = "UploadRequest", description = "업로드할 이미지 요청")
    private static class UploadRequest {
        @Schema(description = "이미지 파일 (jpg, png, gif)", type = "string", format = "binary")
        public MultipartFile file;
    }
}
