package com.fitmate.backend.routine.ai.dto.custom.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CustomSummaryInputDto {
    List<AiCustomSummaryExerciseDto> exercises;

    public static CustomSummaryInputDto of(List<AiCustomSummaryExerciseDto> summaryRequestDtos) {
        return new CustomSummaryInputDto(summaryRequestDtos);
    }
}
