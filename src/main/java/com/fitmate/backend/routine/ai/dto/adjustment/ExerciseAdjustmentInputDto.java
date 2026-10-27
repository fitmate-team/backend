package com.fitmate.backend.routine.ai.dto.adjustment;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExerciseAdjustmentInputDto {

    private List<ExerciseAdjustmentInputItemDto> exerciseRecords;

    public static ExerciseAdjustmentInputDto of(
            List<ExerciseAdjustmentInputItemDto> exerciseRecords
    ) {
        return new ExerciseAdjustmentInputDto(exerciseRecords);
    }
}