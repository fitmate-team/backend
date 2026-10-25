package com.fitmate.backend.routine.ai.dto.weekly;

import com.fitmate.backend.routine.ai.dto.common.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.common.AiRecentExerciseRecordDto;
import com.fitmate.backend.routine.ai.dto.common.AiUserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WeeklyRoutineInputDto {
    private AiUserProfileDto userProfile;
    private List<AiEligibleExerciseDto> eligibleExercises;
    private List<AiRecentExerciseRecordDto> recentExerciseRecords;

    public static WeeklyRoutineInputDto of(AiUserProfileDto userProfile,
                                           List<AiEligibleExerciseDto> eligibleExercises,
                                           List<AiRecentExerciseRecordDto> recentExerciseRecords) {
        return new WeeklyRoutineInputDto(userProfile, eligibleExercises, recentExerciseRecords);
    }
}
