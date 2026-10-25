package com.fitmate.backend.routine.ai.dto.common;

import com.fitmate.backend.exercise.domain.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiEligibleExerciseDto {

    private String exerciseCode;

    private String nameKo;

    private ExerciseType exerciseType;

    private PrimaryMuscle primaryMuscle;

    private MovementPattern movementPattern;

    private ExerciseBaselineType baselineRecordType;


    public static AiEligibleExerciseDto from(Exercise exercise) {
        return new AiEligibleExerciseDto(exercise.getExerciseCode(),
                                         exercise.getNameKo(),
                                         exercise.getExerciseType(),
                                         exercise.getPrimaryMuscle(),
                                         exercise.getMovementPattern(),
                                         exercise.getBaselineRecordType());
    }

}
