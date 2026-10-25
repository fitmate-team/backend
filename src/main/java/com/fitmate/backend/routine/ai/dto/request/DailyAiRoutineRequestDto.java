package com.fitmate.backend.routine.ai.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DailyAiRoutineRequestDto {

    @NotNull
    @Positive
    private Long environmentId;

    @NotNull
    @Positive
    private Integer sessionMinutes;
}