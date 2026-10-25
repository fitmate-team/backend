package com.fitmate.backend.routine.ai.dto.custom.exercise;

import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiCustomExerciseUserProfileDto {

    private ExerciseLevel exerciseLevel;
    private CurrentExerciseStatus currentExerciseStatus;
    private PrimaryGoal primaryGoal;
    private GoalStrategy secondaryGoal;
    private ExerciseLocation exerciseLocation;

    public static AiCustomExerciseUserProfileDto of(MemberProfile profile,
                                                    WorkoutEnvironment environment) {
        return new AiCustomExerciseUserProfileDto(profile.getExerciseLevel(),
                                                  profile.getCurrentExerciseStatus(),
                                                  profile.getPrimaryGoal(),
                                                  profile.getGoalStrategy(),
                                                  environment.getLocationType());
    }

}