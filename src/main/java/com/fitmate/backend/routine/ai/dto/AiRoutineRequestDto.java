package com.fitmate.backend.routine.ai.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiRoutineRequestDto {
    private AiUserProfileDto userProfile;
    private List<AiEligibleExerciseDto> eligibleExercises;
    private List<AiRecentExerciseRecordDto> recentExerciseRecords;

    public static AiRoutineRequestDto of(AiUserProfileDto userProfile,
                                         List<AiEligibleExerciseDto> eligibleExercises,
                                         List<AiRecentExerciseRecordDto> recentExerciseRecords) {
        return new AiRoutineRequestDto(userProfile, eligibleExercises, recentExerciseRecords);
    }
}
