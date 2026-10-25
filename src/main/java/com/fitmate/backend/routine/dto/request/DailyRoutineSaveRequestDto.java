package com.fitmate.backend.routine.dto.request;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.domain.MainTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "AI가 재추천한 당일 운동 루틴 저장 요청")
public class DailyRoutineSaveRequestDto {

    @NotNull
    @Positive
    @Schema(description = "변경할 운동 환경 ID", example = "4")
    private Long environmentId;

    @NotNull
    @Positive
    @Schema(description = "변경된 1회 운동 가능 시간(분)", example = "45")
    private Integer sessionMinutes;

    @NotNull
    @Schema(description = "변경된 오늘의 주요 운동 부위", example = "FULL_BODY")
    private MainTarget mainTarget;

    @NotNull
    @Positive
    @Schema(description = "재추천된 예상 운동 시간(분)", example = "30")
    private Integer estimatedMinutes;

    @NotEmpty
    @Schema(description = "기존 운동과 대체 운동의 1:1 대응 목록 (배열 순서가 운동 순서)")
    private List<@NotNull @Valid ReplacementDto> replacements;

    // 기존 당일 루틴 수정
    public void updateDailyRoutine(DailyRoutine dailyRoutine, WorkoutEnvironment environment) {
        dailyRoutine.updateRoutine(this.mainTarget, environment, this.estimatedMinutes);
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "기존 운동과 재추천 운동의 대응 정보")
    public static class ReplacementDto {

        @NotBlank
        @Schema(description = "기존 오늘 루틴의 운동 코드", example = "EX_LEGS_DB_GOBLET_SQUAT")
        private String originalExerciseCode;

        @NotNull
        @Valid
        @Schema(description = "기존 운동을 대체하거나 그대로 유지할 운동 정보")
        private ExerciseDto replacementExercise;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "재추천된 개별 운동 처방 정보")
    public static class ExerciseDto {

        @NotBlank
        @Schema(description = "Exercise Master에 등록된 운동 코드", example = "EX_LEGS_AIR_SQUAT")
        private String exerciseCode;

        @NotBlank
        @Schema(description = "운동 마스터에 등록된 한글 운동명", example = "맨몸 스쿼트")
        private String nameKo;

        @Positive
        @Schema(description = "추천 중량(kg). 기록이 없거나 중량을 사용하지 않으면 null", example = "22")
        private BigDecimal weightKg;

        @Positive
        @Schema(description = "목표 반복 횟수. 시간 기반 운동은 null", example = "10")
        private Integer reps;

        @NotNull
        @Positive
        @Schema(description = "운동 세트 수. CARDIO는 1", example = "3")
        private Integer sets;

        @Positive
        @Schema(description = "시간 기반 운동의 수행 시간(초). 반복 기반 운동은 null", example = "45")
        private Integer durationSeconds;

        @NotNull
        @PositiveOrZero
        @Schema(description = "세트 간 휴식 시간(초). CARDIO는 0", example = "90")
        private Integer restSeconds;

        // 운동 엔티티 생성
        public ExerciseRoutine toExerciseRoutine(DailyRoutine dailyRoutine,
                                                 Exercise exercise,
                                                 Integer exerciseOrder) {
            return ExerciseRoutine.builder()
                    .dailyRoutine(dailyRoutine)
                    .exercise(exercise)
                    .exerciseOrder(exerciseOrder)
                    .weight(this.weightKg)
                    .reps(this.reps)
                    .setCount(this.sets)
                    .durationSeconds(this.durationSeconds)
                    .restSeconds(this.restSeconds)
                    .build();
        }
    }
}
