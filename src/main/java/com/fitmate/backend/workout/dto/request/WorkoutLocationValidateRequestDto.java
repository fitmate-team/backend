package com.fitmate.backend.workout.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutLocationValidateRequestDto {

    @NotNull(message = "운동 환경 ID는 필수입니다.")
    private Long workoutEnvironmentId;
}
