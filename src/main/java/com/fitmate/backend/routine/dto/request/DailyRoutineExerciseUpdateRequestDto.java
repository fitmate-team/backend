package com.fitmate.backend.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "데일리 루틴의 운동 목록 전체 수정 요청")
public class DailyRoutineExerciseUpdateRequestDto {

    @NotEmpty
    @Schema(description = "최종 확정한 운동 목록. 배열 순서가 운동 순서입니다.")
    private List<@NotNull @Valid ExerciseDto> exercises;

    @Getter
    @NoArgsConstructor
    @Schema(description = "수정할 개별 운동 및 처방값")
    public static class ExerciseDto {

        @NotBlank
        @Schema(
                description = "Exercise Master에 등록된 운동 코드",
                example = "EX_LEGS_DB_GOBLET_SQUAT"
        )
        private String exerciseCode;

        @Positive
        @Schema(
                description = "운동 중량(kg). 중량이 없거나 해당하지 않으면 null입니다.",
                example = "10"
        )
        private Integer weightKg;

        @Positive
        @Schema(
                description = "세트당 목표 반복 횟수. 시간 기반 운동은 null입니다.",
                example = "12"
        )
        private Integer reps;

        @NotNull
        @Positive
        @Schema(
                description = "목표 세트 수. 유산소 운동은 1입니다.",
                example = "3"
        )
        private Integer sets;

        @Positive
        @Schema(
                description = "유산소 운동의 전체 수행 시간(초). 반복 기반 운동은 null입니다.",
                example = "1200"
        )
        private Integer durationSeconds;

        @NotNull
        @PositiveOrZero
        @Schema(
                description = "세트 간 휴식 시간(초). 유산소 운동은 0입니다.",
                example = "60"
        )
        private Integer restSeconds;
    }
}