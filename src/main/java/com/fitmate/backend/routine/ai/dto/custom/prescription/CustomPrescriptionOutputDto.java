package com.fitmate.backend.routine.ai.dto.custom.prescription;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CustomPrescriptionOutputDto {
    private List<PrescriptionDto> prescriptions;

    @Getter
    @NoArgsConstructor
    public static class PrescriptionDto {
        private String exerciseCode;
        private Integer weightKg;
        private Integer reps;
        private Integer sets;
        private Integer durationSeconds;
        private Integer restSeconds;
    }
}
