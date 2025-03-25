package com.p4th.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // JWT, 인증 관련
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 10100, "error.authenticationFailed"),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, 10101, "error.unauthorizedAccess"),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, 10102, "error.loginRequired"),
    BLOCKED_USER(HttpStatus.FORBIDDEN, 10103, "error.blockedUser"),
    BLOCKED_IP(HttpStatus.FORBIDDEN, 10104, "error.blockedIp"),

    // 회원가입/로그인 관련
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, 10200, "error.userNotFound"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 10201, "error.invalidPassword"),
    INVALID_PASSCODE(HttpStatus.BAD_REQUEST, 10202, "error.invalidPasscode"),
    NICKNAME_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 10203, "error.nicknameChangeNotAllowed"),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 10204, "error.nicknameAlreadyExists"),

    // 게시글 관련
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, 20100, "error.postNotFound"),
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 20101, "error.s3UploadFailed"),

    // 댓글 관련
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 20201, "error.commentNotFound"),

    // 스크랩 관련
    SCRAP_FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, 20300, "error.scrapFolderNotFound"),
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND, 20301, "error.scrapNotFound"),
    SCRAP_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 20302, "error.scrapAlreadyExists"),

    //카테고리 관련
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, 20401, "error.categoryNotFound"),
    DUPLICATE_CATEGORY_NAME(HttpStatus.BAD_REQUEST, 20402, "error.duplicateCategoryName"),

    //게시판 관련
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, 20501, "error.boardNotFound"),
    DUPLICATE_BOARD_NAME(HttpStatus.BAD_REQUEST, 20502, "error.duplicateBoardName"),

    //배너 관련
    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, 20601, "error.bannerNotFound"),

    //신고 관련
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, 20701, "error.reportNotFound"),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, 30100, "error.invalidInput"),
    ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, 30101, "error.alreadyProcessed"),

    // 내부 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 40100, "error.internalServerError");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
