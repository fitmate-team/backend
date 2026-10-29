package com.fitmate.backend.routine.ai.dto.adjustment;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExerciseAdjustmentOutputItemDto {

    private Long exerciseId;

    private BigDecimal weightKg;

    private Integer reps;

    private Integer setCount;

    private Integer durationSeconds;

    private String reason;
}