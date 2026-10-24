package com.fitmate.backend.routine.ai.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@NoArgsConstructor
public class WeeklyGenerationRequestDto {

    @NotEmpty
    private List<@NotNull DayOfWeek> selectedDays;

    @NotNull
    @Positive
    private Integer sessionMinutes;

    @NotNull
    private Long environmentId;
}
