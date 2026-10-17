package com.fitmate.backend.routine.dto.ai;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmate.backend.exercise.domain.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
