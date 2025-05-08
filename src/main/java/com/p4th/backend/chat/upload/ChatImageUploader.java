package com.p4th.backend.chat.upload;

import org.springframework.web.multipart.MultipartFile;

public interface ChatImageUploader {
    String upload(MultipartFile file, String roomId);
}