package com.fitmate.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


// 프로젝트 전체에서 발생하는 예외 감시
@RestControllerAdvice // 프로젝트 전역에서 발생하는 예외를 잡는 관제탑임을 선언
@Slf4j
public class GlobalExceptionHandler {

    // CustomException
    @ExceptionHandler(CustomException.class) // 특정 예외를 지정하줌 (CustomException)
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException e) {
        // 1. e에서 ErrorCode 꺼내기
        ErrorCode errorCode = e.getErrorCode();

        // 2. HTTP 상태 코드 꺼내기
        HttpStatus httpStatus = errorCode.getHttpStatus();

        // 3, Dto, 에러 코드명 담아서 보냄
        return ResponseEntity // 스프링 전용 응답 객체
                .status(httpStatus)
                .body(new ErrorResponseDto(e.getMessage(), errorCode.name()));
    }

    // Validation Exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponseDto(errorMessage, "INVALID_INPUT"));
    }

    // 파일 크기 제한
//    @ExceptionHandler(MaxUploadSizeExceededException.class)
//    protected ResponseEntity<ErrorResponseDto> handleMaxUploadSizeExceededException(
//            MaxUploadSizeExceededException e) {
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(new ErrorResponseDto("파일 크기가 너무 큽니다. (최대 50MB)", "FILE_SIZE_EXCEEDED"));
//    }

    // 그 외 일반적인 에러들 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        log.error("알 수 없는 에러 발생: ", e);
        return ResponseEntity
                .internalServerError() // HTTP 상태 코드 500 (서버 내부 오류)
                .body(new ErrorResponseDto("관리자에게 문의하세요.", "INTERNAL_SERVER_ERROR"));
    }
}
