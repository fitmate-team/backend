package com.fitmate.backend.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "오늘의 운동 중량 설정 요청")
public class RoutineWeightUpdateRequestDto {

    @Schema(description = "오늘의 운동 ID", example = "11")
    @NotNull(message = "오늘의 운동 ID는 필수입니다.")
    private Long dailyRoutineId;

    @Schema(description = "중량을 설정할 운동 목록")
    @Valid
    @NotEmpty(message = "중량을 설정할 운동은 필수입니다.")
    private List<ExerciseWeightDto> exercises;

    @Getter
    @NoArgsConstructor
    @Schema(description = "개별 운동 중량 설정 정보")
    public static class ExerciseWeightDto {

        @Schema(description = "루틴에 포함된 개별 운동 ID", example = "31")
        @NotNull(message = "개별 루틴 운동 ID는 필수입니다.")
        private Long routineExerciseId;

        @Schema(description = "설정할 운동 중량 (kg)", example = "8.0")
        @NotNull(message = "중량 kg은 필수입니다.")
        @DecimalMin(value = "0.0", message = "중량은 0 이상이어야 합니다.")
        private BigDecimal weightKg;
    }
}