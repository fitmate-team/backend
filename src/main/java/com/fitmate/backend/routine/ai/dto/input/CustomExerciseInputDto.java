package com.fitmate.backend.routine.ai.dto.input;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CustomExerciseInputDto {
    private CustomExerciseUserProfileDto userProfile;
    private List<AiEligibleExerciseDto> eligibleExercises;

    public static CustomExerciseInputDto of(CustomExerciseUserProfileDto userProfile,
                                            List<AiEligibleExerciseDto> eligibleExercises) {
        return new CustomExerciseInputDto(userProfile, eligibleExercises);
    }
}
