package com.fitmate.backend.routine.ai.dto.custom.prescription;

import com.fitmate.backend.member.domain.ExerciseBaseline;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiCustomPrescriptionBaselineDto {
    private Integer weightKg;
    private Integer reps;
    private Integer sets;
    private Integer durationSeconds;

    public static AiCustomPrescriptionBaselineDto from(ExerciseBaseline baseline) {
        return new AiCustomPrescriptionBaselineDto(baseline.getWeight(),
                                                   baseline.getReps(),
                                                   baseline.getSets(),
                                                   baseline.getDurationSeconds());
    }
}
