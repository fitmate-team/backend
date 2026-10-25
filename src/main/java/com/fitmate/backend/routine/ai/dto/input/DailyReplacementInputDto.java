package com.fitmate.backend.routine.ai.dto.input;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DailyReplacementInputDto {
    private AiUserProfileDto userProfile;
    private List<AiEligibleExerciseDto> eligibleExercises;
    private List<AiRecentExerciseRecordDto> recentExerciseRecords;
    private List<DailyOriginalExerciseDto> originalExercises;

    public static DailyReplacementInputDto of(AiUserProfileDto userProfile,
                                              List<AiEligibleExerciseDto> eligibleExercises,
                                              List<AiRecentExerciseRecordDto> recentExerciseRecords,
                                              List<DailyOriginalExerciseDto> originalExercises) {
        return new DailyReplacementInputDto(userProfile, eligibleExercises, recentExerciseRecords, originalExercises);
    }
}
