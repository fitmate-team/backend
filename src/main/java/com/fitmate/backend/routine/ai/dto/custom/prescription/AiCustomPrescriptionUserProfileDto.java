package com.fitmate.backend.routine.ai.dto.custom.prescription;

import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.enums.CurrentExerciseStatus;
import com.fitmate.backend.member.domain.enums.ExerciseLevel;
import com.fitmate.backend.member.domain.enums.GoalStrategy;
import com.fitmate.backend.member.domain.enums.PrimaryGoal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiCustomPrescriptionUserProfileDto {
    private ExerciseLevel exerciseLevel;
    private CurrentExerciseStatus currentExerciseStatus;
    private PrimaryGoal primaryGoal;
    private GoalStrategy secondaryGoal;

    public static AiCustomPrescriptionUserProfileDto of(MemberProfile profile) {
        return new AiCustomPrescriptionUserProfileDto(profile.getExerciseLevel(),
                                                      profile.getCurrentExerciseStatus(),
                                                      profile.getPrimaryGoal(),
                                                      profile.getGoalStrategy());
    }
}
