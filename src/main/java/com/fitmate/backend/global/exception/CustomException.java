package com.fitmate.backend.global.exception;

import lombok.Getter;

// RuntimeException 상속받으며, Errorcode 를 담아서 던짐
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
