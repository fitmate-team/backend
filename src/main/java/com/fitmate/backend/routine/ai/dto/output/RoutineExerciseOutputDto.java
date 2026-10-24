package com.fitmate.backend.routine.ai.dto.output;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class RoutineExerciseOutputDto {

    private String exerciseCode;
    private String nameKo;
    private Integer sets;
    private Integer reps;
    private Integer durationSeconds;
    private BigDecimal weightKg;
    private Integer restSeconds;
}
