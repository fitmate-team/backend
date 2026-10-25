package com.fitmate.backend.routine.ai.dto.custom.exercise;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CustomExerciseOutputDto {
    private List<String> exerciseCodes;
}
