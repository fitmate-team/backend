package com.fitmate.backend.routine.ai.dto.input;

import com.fitmate.backend.exercise.domain.MovementPattern;
import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DailyOriginalExerciseDto {
    private Integer exerciseOrder;
    private String exerciseCode;
    private String nameKo;
    private PrimaryMuscle primaryMuscle;
    private MovementPattern movementPattern;
    private Integer sets;
    private Integer reps;
    private BigDecimal weightKg;
    private Integer durationSeconds;
    private Integer restSeconds;

    public static DailyOriginalExerciseDto from(ExerciseRoutine exerciseRoutine) {
        return new DailyOriginalExerciseDto(exerciseRoutine.getExerciseOrder(),
                                            exerciseRoutine.getExercise().getExerciseCode(),
                                            exerciseRoutine.getExercise().getNameKo(),
                                            exerciseRoutine.getExercise().getPrimaryMuscle(),
                                            exerciseRoutine.getExercise().getMovementPattern(),
                                            exerciseRoutine.getSetCount(),
                                            exerciseRoutine.getReps(),
                                            exerciseRoutine.getWeight(),
                                            exerciseRoutine.getDurationSeconds(),
                                            exerciseRoutine.getRestSeconds());
    }
}
