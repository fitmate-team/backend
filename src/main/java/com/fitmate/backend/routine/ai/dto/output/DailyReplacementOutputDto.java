package com.fitmate.backend.routine.ai.dto.output;

import com.fitmate.backend.routine.domain.MainTarget;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DailyReplacementOutputDto {

    private MainTarget mainTarget;
    private Integer estimatedMinutes;
    private List<ReplacementDto> replacements;

    @Getter
    @NoArgsConstructor
    public static class ReplacementDto {

        private String originalExerciseCode;
        private RoutineExerciseOutputDto replacementExercise;
    }
}