package com.fitmate.backend.routine.ai.dto.custom.exercise;

import com.fitmate.backend.routine.ai.dto.common.AiEligibleExerciseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CustomExerciseInputDto {
    private AiCustomExerciseUserProfileDto userProfile;
    private List<AiEligibleExerciseDto> eligibleExercises;

    public static CustomExerciseInputDto of(AiCustomExerciseUserProfileDto userProfile,
                                            List<AiEligibleExerciseDto> eligibleExercises) {
        return new CustomExerciseInputDto(userProfile, eligibleExercises);
    }
}
