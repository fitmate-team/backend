package com.fitmate.backend.routine.ai.dto.custom.exercise;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomAiExerciseRequestDto {

    @NotNull
    @Positive
    private Long environmentId;

}
