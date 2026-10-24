package com.fitmate.backend.routine.dto.request;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.MainTarget;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.domain.WeeklyRoutine;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Schema(description = "AI가 생성한 주간 운동 루틴 저장 요청")
public class WeeklyRoutineSaveRequestDto {

    @NotNull
    @Positive
    @Schema(description = "루틴을 수행할 운동 환경 ID", example = "1")
    private Long environmentId;

    @NotNull
    @Positive
    @Schema(description = "1회 운동 가능 시간(분)", example = "60")
    private Integer sessionMinutes;

    @NotEmpty
    @Schema(description = "저장할 요일별 운동 루틴 목록")
    private List<@NotNull @Valid DailyDto> weeklyPlan;

    public WeeklyRoutine toWeeklyRoutine(Member member,
                                         WorkoutEnvironment environment,
                                         LocalDate weekStartDate) {
        return WeeklyRoutine.builder()
                .member(member)
                .weekStartDate(weekStartDate)
                .sessionMinutes(this.sessionMinutes)
                .workoutEnvironment(environment)
                .availableDays(this.weeklyPlan.stream()
                                       .map(DailyDto::getDay)
                                       .collect(Collectors.toSet()))
                .build();
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "요일별 운동 루틴 정보")
    public static class DailyDto {

        @NotNull
        @Schema(description = "운동 요일", example = "SATURDAY")
        private DayOfWeek day;

        @NotNull
        @Schema(description = "해당 운동일의 주요 운동 부위", example = "FULL_BODY")
        private MainTarget mainTarget;

        @NotBlank
        @Size(max = 255)
        @Schema(description = "운동일의 핵심 구성 설명", example = "전신·하체 중심")
        private String focus;

        @NotNull
        @Positive
        @Schema(description = "예상 운동 소요 시간(분)", example = "52")
        private Integer estimatedMinutes;

        @NotEmpty
        @Schema(description = "해당 운동일에 수행할 운동 목록 (배열 순서가 운동 순서)")
        private List<@NotNull @Valid ExerciseDto> exercises;

        public DailyRoutine toDailyRoutine(WeeklyRoutine weeklyRoutine,
                                           LocalDate routineDate,
                                           WorkoutEnvironment environment) {
            return DailyRoutine.builder()
                    .weeklyRoutine(weeklyRoutine)
                    .routineDate(routineDate)
                    .mainTarget(this.mainTarget)
                    .focus(this.focus)
                    .workoutEnvironment(environment)
                    .estimatedDurationMinutes(this.estimatedMinutes)
                    .build();
        }
    }


    @Getter
    @NoArgsConstructor
    @Schema(description = "개별 운동 처방 정보")
    public static class ExerciseDto {

        @NotBlank
        @Schema(description = "Exercise Master에 등록된 운동 코드", example = "EX_LEGS_DB_GOBLET_SQUAT")
        private String exerciseCode;

        @Positive
        @Schema(description = "추천 중량(kg). 개인 중량 기록이 없거나 중량을 사용하지 않는 운동은 null", example = "22")
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
