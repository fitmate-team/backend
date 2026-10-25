package com.fitmate.backend.routine.ai.dto.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WeeklyGenerationInputDto {
    private AiUserProfileDto userProfile;
    private List<AiEligibleExerciseDto> eligibleExercises;
    private List<AiRecentExerciseRecordDto> recentExerciseRecords;

    public static WeeklyGenerationInputDto of(AiUserProfileDto userProfile,
                                              List<AiEligibleExerciseDto> eligibleExercises,
                                              List<AiRecentExerciseRecordDto> recentExerciseRecords) {
        return new WeeklyGenerationInputDto(userProfile, eligibleExercises, recentExerciseRecords);
    }
}
