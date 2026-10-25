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
    EXERCISE_NOT_FOUND("해당 운동을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_EXERCISE_IDS("유효한 운동 ID 목록을 입력해야 합니다.", HttpStatus.BAD_REQUEST),

    // 오늘의 운동
    TODAY_ROUTINE_NOT_FOUND("오늘의 운동 루틴이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    WORKOUT_LOCATION_RERECOMMENDATION_REQUIRED("선택한 운동 환경에서 수행할 수 없는 운동이 포함되어 있습니다. 루틴 재추천이 필요합니다.",
                                               HttpStatus.CONFLICT),
    WORKOUT_ALREADY_STARTED("이미 시작한 운동입니다.", HttpStatus.CONFLICT),

    // 운동 기록
    WORKOUT_RECORD_NOT_FOUND("해당 운동 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    WORKOUT_ROUTINE_MISMATCH("운동 기록과 요청한 루틴이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    WORKOUT_ALREADY_FINISHED("이미 종료된 운동입니다.", HttpStatus.CONFLICT),
    INVALID_WORKOUT_END_TIME("운동 종료 시각은 시작 시각보다 이전일 수 없습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_WORKOUT_EXERCISE("운동 수행 기록에 중복된 루틴 운동이 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    WORKOUT_EXERCISE_MISMATCH("해당 루틴의 모든 운동을 미수행 운동까지 포함하여 전달해야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_HEART_RATE_RANGE("최소 심박수는 최대 심박수보다 클 수 없습니다.", HttpStatus.BAD_REQUEST),
    MEMBER_PROFILE_NOT_FOUND("회원 프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_WORKOUT_YEAR_MONTH("조회 연도는 1~9999, 월은 1~12 범위로 입력해주세요.", HttpStatus.BAD_REQUEST),

    // 루틴
    ROUTINE_EXERCISE_NOT_FOUND("해당 루틴 운동을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ROUTINE_ACCESS_DENIED("해당 루틴에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NO_AVAILABLE_ROUTINE_DAY("이번 주에 생성 가능한 운동 요일이 없습니다.", HttpStatus.BAD_REQUEST),
    NO_ELIGIBLE_EXERCISE("현재 조건에서 추천 가능한 운동이 없습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_WEEKLY_ROUTINE("이번 주 주간 루틴이 이미 저장되어 있습니다.", HttpStatus.CONFLICT),
    INVALID_WEEKLY_ROUTINE_DAY("이번 주 오늘 이후의 운동 요일만 저장할 수 있습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_WEEKLY_ROUTINE_DAY("주간 루틴에 중복된 운동 요일이 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    ROUTINE_DURATION_EXCEEDED("예상 운동시간이 설정한 운동 가능 시간을 초과합니다.", HttpStatus.BAD_REQUEST),
    WEEKLY_ROUTINE_NOT_FOUND("이번 주 저장된 운동 루틴이 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_DAILY_ROUTINE_SAVE("유효하지 않은 당일 루틴 저장 요청입니다.", HttpStatus.BAD_REQUEST),
    INVALID_FIRST_SELECTED_ROUTINE_DAY("최초 선택한 운동 요일이 주간 루틴에 포함되어 있지 않습니다.", HttpStatus.BAD_REQUEST),
    EXERCISE_NOT_AVAILABLE_IN_ENVIRONMENT("선택한 운동 환경에서 수행할 수 없는 운동이 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_CUSTOM_ROUTINE_PRESCRIPTION("운동 유형에 맞지 않는 운동 처방 정보입니다.", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_CUSTOM_EXERCISE_CANDIDATES("AI 운동 종목 추천을 위한 운동 후보가 3개 미만입니다.", HttpStatus.BAD_REQUEST),

    // 기록
    DUPLICATE_BODY_COMPOSITION("오늘의 신체 기록이 이미 존재합니다.", HttpStatus.CONFLICT),
    EXERCISE_GROWTH_RECORD_NOT_FOUND("최근 6개월 내 해당 운동의 중량 수행 기록이 없습니다.", HttpStatus.NOT_FOUND),

    // AI
    OPENAI_API_ERROR("AI 서비스 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    OPENAI_TIMEOUT("AI 서비스 응답 시간이 초과되었습니다.", HttpStatus.GATEWAY_TIMEOUT),
    OPENAI_INVALID_RESPONSE("AI 서비스가 올바르지 않은 응답을 반환했습니다.", HttpStatus.BAD_GATEWAY),
    OPENAI_RESPONSE_REFUSED("AI가 요청을 처리할 수 없습니다.", HttpStatus.BAD_GATEWAY),

    // etc
    TARGET_WEIGHT_REQUIRED("체중 감량 목표인 경우 목표 체중을 입력해주세요.", HttpStatus.BAD_REQUEST);


    private final String message;
    private final HttpStatus httpStatus;
}
