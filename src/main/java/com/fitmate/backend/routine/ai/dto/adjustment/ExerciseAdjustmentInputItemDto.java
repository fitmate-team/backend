package com.fitmate.backend.routine.ai.dto.adjustment;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.workout.domain.CalculatedDifficulty;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExerciseAdjustmentInputItemDto {

    private Long exerciseId;

    private String exerciseName;

    private ExerciseBaselineType baselineRecordType;

    private CalculatedDifficulty calculatedDifficulty;

    private Integer weightKg;

    private Integer reps;

    private Integer setCount;

    private Integer durationSeconds;

    private Integer completedSetCount;

    private Integer completedDurationSeconds;

    public static ExerciseAdjustmentInputItemDto from(
            WorkoutExerciseRecord workoutExerciseRecord
    ) {
        ExerciseRoutine exerciseRoutine = workoutExerciseRecord.getExerciseRoutine();
        Exercise exercise = exerciseRoutine.getExercise();

        return new ExerciseAdjustmentInputItemDto(
                exercise.getId(),
                exercise.getNameKo(),
                exercise.getBaselineRecordType(),
                workoutExerciseRecord.getCalculatedDifficulty(),
                exerciseRoutine.getWeight(),
                exerciseRoutine.getReps(),
                exerciseRoutine.getSetCount(),
                exerciseRoutine.getDurationSeconds(),
                workoutExerciseRecord.getCompletedSetCount(),
                workoutExerciseRecord.getCompletedDurationSeconds()
        );
    }
}