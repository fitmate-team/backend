package com.fitmate.backend.routine.dto.ai;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.member.domain.ExerciseBaseline;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiRecentExerciseRecordDto {

    private String exerciseCode;

    private Integer weight;

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
