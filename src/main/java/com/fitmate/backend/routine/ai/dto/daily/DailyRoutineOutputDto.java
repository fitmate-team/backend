package com.fitmate.backend.routine.ai.dto.daily;

import com.fitmate.backend.routine.ai.dto.common.ExerciseRoutineOutputDto;
import com.fitmate.backend.routine.domain.MainTarget;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DailyRoutineOutputDto {

    private MainTarget mainTarget;
    private Integer estimatedMinutes;
    private List<ReplacementDto> replacements;

    @Getter
    @NoArgsConstructor
    public static class ReplacementDto {

        private String originalExerciseCode;
        private ExerciseRoutineOutputDto replacementExercise;
    }
}