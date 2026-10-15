package com.fitmate.backend.routine.dto.ai;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
