package com.fitmate.backend.workout.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "운동 시작 요청")
public class WorkoutRecordCreateRequestDto {

    @Schema(description = "오늘의 루틴 ID", example = "28")
    @NotNull(message = "오늘의 루틴 ID는 필수입니다.")
    private Long dailyRoutineId;
}