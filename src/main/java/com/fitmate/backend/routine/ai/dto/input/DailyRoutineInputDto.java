package com.fitmate.backend.routine.ai.dto.input;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DailyRoutineInputDto {
    private AiUserProfileDto userProfile;
    private List<AiEligibleExerciseDto> eligibleExercises;
    private List<AiRecentExerciseRecordDto> recentExerciseRecords;
    private List<AiDailyOriginalExerciseDto> originalExercises;

    public static DailyRoutineInputDto of(AiUserProfileDto userProfile,
                                          List<AiEligibleExerciseDto> eligibleExercises,
                                          List<AiRecentExerciseRecordDto> recentExerciseRecords,
                                          List<AiDailyOriginalExerciseDto> originalExercises) {
        return new DailyRoutineInputDto(userProfile, eligibleExercises, recentExerciseRecords, originalExercises);
    }
}
