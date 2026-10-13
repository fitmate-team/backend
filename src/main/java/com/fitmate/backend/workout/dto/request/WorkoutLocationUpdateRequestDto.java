package com.fitmate.backend.workout.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkoutLocationUpdateRequestDto {

    @NotNull(message = "운동 환경 ID는 필수입니다.")
    private Long workoutEnvironmentId;
}
