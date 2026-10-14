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
    INVALID_EQUIPMENT_CODE("존재하지 않는 운동기구 코드가 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_EXERCISE_CODE("존재하지 않는 운동 코드가 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_RECENT_EXERCISE("최근 운동 기록에 중복된 운동이 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_EXERCISE_BASELINE("운동 유형에 맞지 않는 최근 운동 기록입니다.", HttpStatus.BAD_REQUEST),
    INVALID_WORKOUT_ENVIRONMENT("운동 장소와 운동 환경 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_GOAL_STRATEGY("운동 목표에 맞지 않는 운동 방식입니다.", HttpStatus.BAD_REQUEST),
    WORKOUT_ENVIRONMENT_NOT_FOUND("등록된 운동 환경을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 토큰
    REFRESH_TOKEN_EXPIRED("리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),

    // 운동
    TODAY_ROUTINE_NOT_FOUND("오늘의 운동 루틴이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    WORKOUT_LOCATION_RERECOMMENDATION_REQUIRED("선택한 운동 환경에서 수행할 수 없는 운동이 포함되어 있습니다. 루틴 재추천이 필요합니다.", HttpStatus.CONFLICT),

    // 루틴
    ROUTINE_EXERCISE_NOT_FOUND("해당 루틴 운동을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ROUTINE_EXERCISE_MISMATCH("해당 운동은 요청한 오늘의 운동에 속하지 않습니다.", HttpStatus.BAD_REQUEST),
    ROUTINE_ACCESS_DENIED("해당 루틴에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // etc
    TARGET_WEIGHT_REQUIRED("체중 감량 목표인 경우 목표 체중을 입력해주세요.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;
}
