package com.fitmate.backend.routine.ai.dto.adjustment;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExerciseAdjustmentOutputDto {

    private List<ExerciseAdjustmentOutputItemDto> exercises;

    public static ExerciseAdjustmentOutputDto empty() {
        return new ExerciseAdjustmentOutputDto(List.of());
    }
}