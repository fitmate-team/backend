package com.fitmate.backend.workout.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkoutStartResponseDto {

    @Schema(description = "생성된 운동 기록 ID", example = "2")
    private final Long workoutRecordId;

    public static WorkoutStartResponseDto of(Long workoutRecordId) {
        return new WorkoutStartResponseDto(workoutRecordId);
    }
}