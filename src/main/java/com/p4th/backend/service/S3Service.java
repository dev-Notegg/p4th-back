package com.p4th.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.p4th.backend.config.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final S3Config s3Config;

    /**
     * 단일 파일 업로드
     * @param multipartFile 업로드할 MultipartFile
     * @param dirName 폴더명 (예: "posts")
     * @param fileName 저장할 파일명
     * @return CDN URL로 변환된 업로드 파일 URL
     */
    public String upload(MultipartFile multipartFile, String dirName, String fileName) {
        try {
            Optional<File> converted = convert(multipartFile);
            if (converted.isEmpty()) {
                throw new IllegalArgumentException("MultipartFile -> File 변환 실패");
            }
            File file = converted.get();
            String key = dirName + "/" + fileName;
            amazonS3.putObject(new PutObjectRequest(s3Config.getBucketName(), key, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            removeNewFile(file);
            String url = amazonS3.getUrl(s3Config.getBucketName(), key).toString();
            return convertCdnUrl(url);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            System.out.println("임시 파일 삭제 완료");
        } else {
            System.out.println("임시 파일 삭제 실패");
        }
    }

    /**
     * MultipartFile을 File로 변환
     */
    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    /**
     * S3 URL을 CDN URL로 변환
     */
    public String convertCdnUrl(String url) {
        return url.replace("p4th.kr.object.ncloudstorage.com", "4pth.gcdn.ntruss.com");
    }

    /**
     * S3 업로드 시 생성된 URL을 통해, 파일 키를 추출하여 삭제 처리한다.
     * @param fileUrl 업로드된 파일의 CDN URL
     */
    public void deleteByFileUrl(String fileUrl) {
        if (fileUrl == null) return;

        // CDN URL이 저장되어 있다면, S3의 원본 도메인으로 변환
        String originalUrl = fileUrl;
        if (fileUrl.contains("4pth.gcdn.ntruss.com")) {
            originalUrl = fileUrl.replace("4pth.gcdn.ntruss.com", "kr.object.ncloudstorage.com");
        }

        // S3의 원본 URL 형식: "https://kr.object.ncloudstorage.com/{bucket}/..."
        String prefix = s3Config.getEndPoint() + "/" + s3Config.getBucketName() + "/";
        if (!originalUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("URL 형식이 올바르지 않습니다: " + originalUrl);
        }

        String key = originalUrl.substring(prefix.length());
        amazonS3.deleteObject(s3Config.getBucketName(), key);
    }
}
