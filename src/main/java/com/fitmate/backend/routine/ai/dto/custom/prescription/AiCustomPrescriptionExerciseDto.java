package com.fitmate.backend.routine.ai.dto.custom.prescription;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.exercise.domain.ExerciseType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiCustomPrescriptionExerciseDto {
    private String exerciseCode;
    private String nameKo;
    private ExerciseBaselineType baselineRecordType;
    private ExerciseType exerciseType;
    private List<AiCustomPrescriptionRecordDto> recentRecords;
    private AiCustomPrescriptionBaselineDto baseline;

    public static AiCustomPrescriptionExerciseDto from(Exercise exercise,
                                                       List<AiCustomPrescriptionRecordDto> recentRecords,
                                                       AiCustomPrescriptionBaselineDto baseline) {
        return new AiCustomPrescriptionExerciseDto(exercise.getExerciseCode(),
                                                   exercise.getNameKo(),
                                                   exercise.getBaselineRecordType(),
                                                   exercise.getExerciseType(),
                                                   recentRecords,
                                                   baseline);

    }

}
