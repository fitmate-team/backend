package com.fitmate.backend.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "운동 조정 추천값 적용 요청")
public class ExerciseAdjustmentApplyRequestDto {

    @Schema(
            description = "적용할 중량(kg). 중량을 사용하지 않는 운동은 null",
            example = "22"
    )
    @Positive
    private Integer weightKg;

    @Schema(
            description = "적용할 반복 횟수. 반복 횟수를 사용하지 않는 운동은 null",
            example = "10"
    )
    @Min(1)
    private Integer reps;

    @Schema(
            description = "적용할 세트 수. 시간 기반 운동은 1",
            example = "3"
    )
    @NotNull
    @Min(1)
    private Integer setCount;

    @Schema(
            description = "적용할 운동 시간(초). 시간 기반 운동이 아니면 null",
            example = "1500"
    )
    @Min(1)
    private Integer durationSeconds;
}