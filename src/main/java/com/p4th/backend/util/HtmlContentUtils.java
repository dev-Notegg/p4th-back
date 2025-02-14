package com.p4th.backend.util;

import org.jsoup.Jsoup;

public class HtmlContentUtils {
    
    /**
     * HTML 문자열에서 태그를 제거하고 순수 텍스트를 추출한 후, maxLength 길이로 자른다.
     * @param htmlContent 원본 HTML 콘텐츠
     * @param maxLength 최대 길이
     * @return 순수 텍스트 (최대 maxLength 길이)
     */
    public static String extractPlainText(String htmlContent, int maxLength) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }
        String plainText = Jsoup.parse(htmlContent).text();
        if (plainText.length() > maxLength) {
            plainText = plainText.substring(0, maxLength);
        }
        return plainText;
    }
}
