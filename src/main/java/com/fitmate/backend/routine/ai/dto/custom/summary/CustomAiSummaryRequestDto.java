package com.fitmate.backend.routine.ai.dto.custom.summary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class CustomAiSummaryRequestDto {

    @NotEmpty
    private List<@NotNull @Valid ExerciseDto> exercises;

    @Getter
    @NoArgsConstructor
    public static class ExerciseDto {

        @NotBlank
        private String exerciseCode;

        @Positive
        private BigDecimal weightKg;

        @Positive
        private Integer reps;

        @NotNull
        @Positive
        private Integer sets;

        @Positive
        private Integer durationSeconds;

        @NotNull
        @PositiveOrZero
        private Integer restSeconds;
    }
}
