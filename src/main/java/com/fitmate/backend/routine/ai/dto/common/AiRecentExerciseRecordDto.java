package com.fitmate.backend.routine.ai.dto.common;

import com.fitmate.backend.member.domain.ExerciseBaseline;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiRecentExerciseRecordDto {

    private String exerciseCode;

    private Integer weightKg;

    private Integer reps;

    private Integer sets;

    private Integer durationSeconds;

    public static AiRecentExerciseRecordDto from(ExerciseBaseline baseline) {
        return new AiRecentExerciseRecordDto(baseline.getExercise().getExerciseCode(),
                                             baseline.getWeight(),
                                             baseline.getReps(),
                                             baseline.getSets(),
                                             baseline.getDurationSeconds());
    }
}
