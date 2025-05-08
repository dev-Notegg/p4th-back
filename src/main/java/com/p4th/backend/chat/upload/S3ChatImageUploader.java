package com.p4th.backend.chat.upload;

import com.p4th.backend.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "chat.upload.type", havingValue = "s3", matchIfMissing = true)
public class S3ChatImageUploader implements ChatImageUploader {

    private final S3Service s3Service;

    @Override
    public String upload(MultipartFile file, String roomId) {
        String ext = getExtension(Objects.requireNonNull(file.getOriginalFilename()));
        String fileName = roomId + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        return s3Service.upload(file, "chat-images", fileName);
    }

    private String getExtension(String originalFilename) {
        int idx = originalFilename.lastIndexOf('.');
        if (idx < 0) throw new IllegalArgumentException("확장자가 없는 파일입니다.");
        return originalFilename.substring(idx + 1);
    }
}
