package com.p4th.backend.util;

import com.p4th.backend.service.S3Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlImageUtils {

    /**
     * HTML 콘텐츠 내의 모든 <img> 태그 중 조건에 맞는 첫 번째 이미지의 src를 반환한다.
     * 조건:
     *   - src가 jpg, jpeg, png, gif, bmp 확장자로 끝나거나,
     *   - src가 HTTP URL로 시작하면서 (비디오/임베디드 관련 키워드가 없는 경우)
     *
     * @param htmlContent HTML 문자열
     * @return 조건에 맞는 첫 번째 이미지 URL, 없으면 null
     */
    public static String extractFirstImageUrl(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return null;
        }
        Document doc = Jsoup.parse(htmlContent);
        Elements imgs = doc.select("img[src]");
        for (Element img : imgs) {
            String src = img.attr("src");
            if (isImageUrl(src)) {
                return src;
            }
        }
        return null;
    }

    /**
     * HTML 콘텐츠 내의 <img> 태그 중, 조건에 맞는 이미지 태그의 개수를 반환한다.
     *
     * @param htmlContent HTML 문자열
     * @return 조건에 맞는 이미지 태그 개수
     */
    public static int countInlineImages(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return 0;
        }
        Document doc = Jsoup.parse(htmlContent);
        Elements imgs = doc.select("img[src]");
        int count = 0;
        for (Element img : imgs) {
            String src = img.attr("src");
            if (isImageUrl(src)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 주어진 src URL이 이미지 URL로 판단되는지 확인한다.
     * 조건:
     *   1. src가 jpg, jpeg, png, gif, bmp 확장자(쿼리스트링 포함)로 끝나거나,
     *   2. src가 HTTP URL로 시작하고, "youtube", "youtu.be", "vimeo", "dailymotion" 등의 키워드가 포함되지 않으면 이미지로 판단.
     *
     * @param src 이미지 URL
     * @return 조건에 맞으면 true, 아니면 false
     */
    public static boolean isImageUrl(String src) {
        if (src == null || src.isEmpty()) {
            return false;
        }
        // 조건 1: 파일 확장자 검사 (대소문자 무시)
        if (src.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$")) {
            return true;
        }
        // 조건 2: HTTP URL이고, 비디오/임베디드 관련 키워드가 없는 경우
        return src.startsWith("http") &&
                !src.toLowerCase().contains("youtube") &&
                !src.toLowerCase().contains("youtu.be") &&
                !src.toLowerCase().contains("vimeo") &&
                !src.toLowerCase().contains("dailymotion");
    }

    /**
     * HTML 콘텐츠 내의 inline 미디어 처리
     * - base64 인코딩된 이미지 태그를 찾아 S3에 업로드 후 URL로 교체한다.
     *
     * @param content   원본 HTML 콘텐츠
     * @param boardId   게시판 ID (이미지 저장 경로에 사용)
     * @param s3Service S3Service 인스턴스 (메서드 호출 시 전달)
     * @return 처리된 HTML 콘텐츠
     */
    public static String processInlineMedia(String content, String boardId, S3Service s3Service) {
        Document doc = Jsoup.parse(content);
        Elements imgTags = doc.select("img[src^=data:image/]");
        for (Element img : imgTags) {
            String dataUri = img.attr("src");
            int commaIndex = dataUri.indexOf(",");
            if (commaIndex > 0) {
                String metadata = dataUri.substring(0, commaIndex);  // 예: "data:image/png;base64"
                String base64Data = dataUri.substring(commaIndex + 1);
                // 확장자 추출 (예: image/png -> png)
                String ext = metadata.substring(metadata.indexOf("/") + 1, metadata.indexOf(";")).toLowerCase();
                byte[] mediaBytes = java.util.Base64.getDecoder().decode(base64Data);
                // 파일명 생성 (예: ULID + 확장자)
                String attachmentId = ULIDUtil.getULID();
                String fileName = attachmentId + "." + ext;
                String keyDir = "posts/" + boardId;
                // S3Service의 byte[] 업로드 메서드 호출
                String fileUrl = s3Service.upload(mediaBytes, keyDir, fileName);
                // 이미지 태그의 src 값을 업로드된 URL로 교체
                img.attr("src", fileUrl);
            }
        }
        return doc.body().html();
    }
}
