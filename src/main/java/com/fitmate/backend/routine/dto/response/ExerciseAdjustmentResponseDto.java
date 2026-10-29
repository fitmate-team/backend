package com.fitmate.backend.routine.dto.response;

import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentInputItemDto;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentOutputItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "운동별 조정 추천 응답")
public class ExerciseAdjustmentResponseDto {

    @Schema(description = "운동 기록 ID", example = "10")
    private Long workoutRecordId;

    @Schema(description = "운동별 조정 추천 목록")
    private List<ExerciseAdjustmentItem> exercises;

    public static ExerciseAdjustmentResponseDto of(
            Long workoutRecordId,
            List<ExerciseAdjustmentItem> exercises
    ) {
        return new ExerciseAdjustmentResponseDto(workoutRecordId, exercises);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "운동별 조정 추천 정보")
    public static class ExerciseAdjustmentItem {

        @Schema(description = "운동 ID", example = "1")
        private Long exerciseId;

        @Schema(description = "운동 이름", example = "숄더프레스")
        private String exerciseName;

        @Schema(
                description = "운동 기록 방식",
                example = "WEIGHT_REPS_SETS"
        )
        private ExerciseBaselineType baselineRecordType;

        @Schema(description = "현재 운동값")
        private ExerciseValues current;

        @Schema(description = "AI가 추천한 운동값")
        private ExerciseValues recommended;

        @Schema(description = "변경된 항목과 방향 목록")
        private List<ExerciseChange> changes;

        @Schema(
                description = "AI 조정 추천 이유",
                example = "이번 운동을 쉽게 수행하여 중량을 높였다."
        )
        private String reason;

        public static ExerciseAdjustmentItem of(
                ExerciseAdjustmentInputItemDto input,
                ExerciseAdjustmentOutputItemDto output
        ) {
            ExerciseValues current = ExerciseValues.of(
                    input.getWeightKg(),
                    input.getReps(),
                    input.getSetCount(),
                    input.getDurationSeconds()
            );

            ExerciseValues recommended = ExerciseValues.of(
                    output.getWeightKg(),
                    output.getReps(),
                    output.getSetCount(),
                    output.getDurationSeconds()
            );

            List<ExerciseChange> changes = createChanges(current, recommended);

            return new ExerciseAdjustmentItem(
                    input.getExerciseId(),
                    input.getExerciseName(),
                    input.getBaselineRecordType(),
                    current,
                    recommended,
                    changes,
                    output.getReason()
            );
        }

        // 현재 값과 추천값을 비교해 변경된 항목만 목록에 추가한다.
        private static List<ExerciseChange> createChanges(
                ExerciseValues current,
                ExerciseValues recommended
        ) {
            List<ExerciseChange> changes = new ArrayList<>();

            addChange(
                    changes,
                    AdjustmentField.WEIGHT,
                    current.getWeightKg(),
                    recommended.getWeightKg()
            );

            addChange(
                    changes,
                    AdjustmentField.REPS,
                    current.getReps(),
                    recommended.getReps()
            );

            addChange(
                    changes,
                    AdjustmentField.SET_COUNT,
                    current.getSetCount(),
                    recommended.getSetCount()
            );

            addChange(
                    changes,
                    AdjustmentField.DURATION,
                    current.getDurationSeconds(),
                    recommended.getDurationSeconds()
            );

            return changes;
        }

        // 비교할 수 있는 두 값이 모두 있고, 값이 달라진 경우에만 추가한다.
        private static <T extends Comparable<? super T>> void addChange(
                List<ExerciseChange> changes,
                AdjustmentField field,
                T currentValue,
                T recommendedValue
        ) {
            if (currentValue == null || recommendedValue == null) {
                return;
            }

            int comparison = recommendedValue.compareTo(currentValue);

            if (comparison > 0) {
                changes.add(ExerciseChange.of(field, AdjustmentDirection.INCREASE));
            } else if (comparison < 0) {
                changes.add(ExerciseChange.of(field, AdjustmentDirection.DECREASE));
            }
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "운동 수행 값")
    public static class ExerciseValues {

        @Schema(
                description = "중량(kg). 중량을 사용하지 않는 운동은 null",
                example = "40.0",
                nullable = true
        )
        private Integer weightKg;

        @Schema(
                description = "반복 횟수. 반복 횟수를 사용하지 않는 운동은 null",
                example = "8",
                nullable = true
        )
        private Integer reps;

        @Schema(
                description = "세트 수. 시간 기반 운동은 1",
                example = "4"
        )
        private Integer setCount;

        @Schema(
                description = "운동 시간(초). 시간 기반 운동이 아니면 null",
                example = "1500",
                nullable = true
        )
        private Integer durationSeconds;

        public static ExerciseValues of(
                Integer weightKg,
                Integer reps,
                Integer setCount,
                Integer durationSeconds
        ) {
            return new ExerciseValues(weightKg, reps, setCount, durationSeconds);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "변경된 운동 항목과 방향")
    public static class ExerciseChange {

        @Schema(description = "변경된 항목", example = "WEIGHT")
        private AdjustmentField field;

        @Schema(description = "변경 방향", example = "INCREASE")
        private AdjustmentDirection direction;

        public static ExerciseChange of(
                AdjustmentField field,
                AdjustmentDirection direction
        ) {
            return new ExerciseChange(field, direction);
        }
    }

    @Schema(description = "변경된 운동 항목")
    public enum AdjustmentField {
        WEIGHT,
        REPS,
        SET_COUNT,
        DURATION
    }

    @Schema(description = "변경 방향")
    public enum AdjustmentDirection {
        INCREASE,
        DECREASE
    }
}