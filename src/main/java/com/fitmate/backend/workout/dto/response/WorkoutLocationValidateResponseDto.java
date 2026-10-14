package com.fitmate.backend.workout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkoutLocationValidateResponseDto {

    // 사용자가 새롭게 선택한 운동 환경 ID
    private Long workoutEnvironmentId;

    // 현재 루틴이 기준으로 삼고 있는 운동 환경 이름
    private String currentRoutineLocationName;

    // 선택한 운동 환경에 맞춰 루틴 재추천이 필요한지 여부
    private boolean requiresRerecommendation;

    public static WorkoutLocationValidateResponseDto of(
            Long workoutEnvironmentId,
            String currentRoutineLocationName,
            boolean requiresRerecommendation
    ) {
        return new WorkoutLocationValidateResponseDto(
                workoutEnvironmentId,
                currentRoutineLocationName,
                requiresRerecommendation
        );
    }
}