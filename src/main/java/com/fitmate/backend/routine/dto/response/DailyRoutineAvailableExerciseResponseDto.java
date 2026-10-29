package com.fitmate.backend.routine.dto.response;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.equipment.domain.EquipmentCategory;
import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "데일리 루틴에서 선택 가능한 운동 목록 응답")
public class DailyRoutineAvailableExerciseResponseDto {

    @Schema(description = "데일리 루틴 ID", example = "15")
    private final Long dailyRoutineId;

    @Schema(description = "해당 데일리 루틴의 운동 환경에서 가능한 운동 목록")
    private final List<ExerciseItem> exercises;

    public static DailyRoutineAvailableExerciseResponseDto of(
            Long dailyRoutineId,
            List<Exercise> availableExercises,
            Set<Long> includedExerciseIds
    ) {
        List<ExerciseItem> exercises = availableExercises.stream()
                .map(exercise -> ExerciseItem.from(
                        exercise,
                        includedExerciseIds.contains(exercise.getId())
                ))
                .collect(Collectors.toList());

        return new DailyRoutineAvailableExerciseResponseDto(
                dailyRoutineId,
                exercises
        );
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "선택 가능한 운동 정보")
    public static class ExerciseItem {

        @Schema(description = "운동 ID", example = "1")
        private final Long exerciseId;

        @Schema(
                description = "Exercise Master에 등록된 운동 코드",
                example = "EX_LEGS_DB_GOBLET_SQUAT"
        )
        private final String exerciseCode;

        @Schema(description = "운동 한글 이름", example = "벤치 프레스")
        private final String nameKo;

        @Schema(description = "주요 운동 부위", example = "CHEST")
        private final PrimaryMuscle primaryMuscle;

        @Schema(
                description = "해당 운동에 연결된 기구의 분류 목록",
                example = "[\"FREE_WEIGHT\", \"BENCH_RACK\"]"
        )
        private final Set<EquipmentCategory> equipmentCategories;

        @Schema(
                description = "해당 데일리 루틴에 이미 포함된 운동인지 여부",
                example = "true"
        )
        private final boolean includedInDailyRoutine;

        public static ExerciseItem from(
                Exercise exercise,
                boolean includedInDailyRoutine
        ) {
            Set<EquipmentCategory> equipmentCategories =
                    exercise.getEquipment().stream()
                            .map(Equipment::getCategory)
                            .collect(Collectors.toSet());

            return new ExerciseItem(
                    exercise.getId(),
                    exercise.getExerciseCode(),
                    exercise.getNameKo(),
                    exercise.getPrimaryMuscle(),
                    equipmentCategories,
                    includedInDailyRoutine
            );
        }
    }
}