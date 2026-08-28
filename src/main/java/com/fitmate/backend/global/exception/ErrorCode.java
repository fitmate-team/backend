package com.fitmate.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// 에러 목록 Enum
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 회원가입
    DUPLICATE_LOGIN_ID("이미 존재하는 ID 입니다.", HttpStatus.CONFLICT),
    MEMBER_NOT_FOUND("해당 회원이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);


    private final String message;
    private final HttpStatus httpStatus;
}
