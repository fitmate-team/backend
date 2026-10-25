package com.fitmate.backend.routine.ai.dto.input;

import com.fitmate.backend.member.domain.BodyComposition;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.*;
import com.fitmate.backend.routine.ai.dto.input.AiUserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomExerciseUserProfileDto {

    private ExerciseLevel exerciseLevel;
    private CurrentExerciseStatus currentExerciseStatus;
    private PrimaryGoal primaryGoal;
    private GoalStrategy secondaryGoal;
    private ExerciseLocation exerciseLocation;

    public static CustomExerciseUserProfileDto of(MemberProfile profile,
                                                  WorkoutEnvironment environment) {
        return new CustomExerciseUserProfileDto(profile.getExerciseLevel(),
                                                profile.getCurrentExerciseStatus(),
                                                profile.getPrimaryGoal(),
                                                profile.getGoalStrategy(),
                                                environment.getLocationType());
    }

}