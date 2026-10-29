package com.fitmate.backend.routine.ai.dto.custom.prescription;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CustomPrescriptionInputDto {
    private AiCustomPrescriptionUserProfileDto userProfile;
    private List<AiCustomPrescriptionExerciseDto> exercises;

    public static CustomPrescriptionInputDto of(AiCustomPrescriptionUserProfileDto userProfile,
                                            List<AiCustomPrescriptionExerciseDto> exercises) {
        return new CustomPrescriptionInputDto(userProfile, exercises);
    }
}
