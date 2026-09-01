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
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 토큰
    REFRESH_TOKEN_EXPIRED("리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),

    // etc
    TARGET_WEIGHT_REQUIRED("체중 감량 목표인 경우 목표 체중을 입력해주세요.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;
}
