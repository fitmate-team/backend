package com.fitmate.backend.routine.ai.dto.custom.summary;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiCustomSummaryExerciseDto {

    // DB 정보
    private String nameKo;
    private ExerciseType exerciseType;
    private PrimaryMuscle primaryMuscle;
    private ExerciseBaselineType baselineRecordType;

    // RequestDto에서 가져온 값
    private String exerciseCode;
    private Integer weightKg;
    private Integer reps;
    private Integer sets;
    private Integer durationSeconds;
    private Integer restSeconds;

    public static AiCustomSummaryExerciseDto from(Exercise exercise,
                                                  CustomAiSummaryRequestDto.ExerciseDto requestDto) {
        return new AiCustomSummaryExerciseDto(exercise.getNameKo(),
                                              exercise.getExerciseType(),
                                              exercise.getPrimaryMuscle(),
                                              exercise.getBaselineRecordType(),
                                              requestDto.getExerciseCode(),
                                              requestDto.getWeightKg(),
                                              requestDto.getReps(),
                                              requestDto.getSets(),
                                              requestDto.getDurationSeconds(),
                                              requestDto.getRestSeconds());
    }
}
