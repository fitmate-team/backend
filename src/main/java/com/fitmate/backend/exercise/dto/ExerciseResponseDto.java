package com.fitmate.backend.exercise.dto;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.exercise.domain.ExerciseType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExerciseResponseDto {
    private String exerciseCode;
    private String nameKo;
    private ExerciseType exerciseType;
    private ExerciseBaselineType baselineRecordType;

    public static ExerciseResponseDto from(Exercise exercise) {
        return new ExerciseResponseDto(exercise.getExerciseCode(),
                                       exercise.getNameKo(),
                                       exercise.getExerciseType(),
                                       exercise.getBaselineRecordType());
    }
}
