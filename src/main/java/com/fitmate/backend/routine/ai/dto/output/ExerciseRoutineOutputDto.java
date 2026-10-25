package com.fitmate.backend.routine.ai.dto.output;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// 재사용
public class ExerciseRoutineOutputDto {

    private String exerciseCode;
    private String nameKo;
    private Integer sets;
    private Integer reps;
    private Integer durationSeconds;
    private BigDecimal weightKg;
    private Integer restSeconds;
}
