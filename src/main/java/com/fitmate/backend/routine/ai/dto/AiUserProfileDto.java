package com.fitmate.backend.routine.ai.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.fitmate.backend.member.domain.BodyComposition;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiUserProfileDto {

    private Gender gender;

    private Integer age;

    private Double heightCm;

    private Double weightKg;

    private Double skeletalMuscleMassKg;

    private Double bodyFatPercentage;

    private Double bodyFatMassKg;

    private ExerciseLevel exerciseLevel;

    private CurrentExerciseStatus currentExerciseStatus;

    private PrimaryGoal primaryGoal;

    private GoalStrategy secondaryGoal;

    private Integer weeklyFrequency;

    private List<DayOfWeek> availableDays;

    private Integer sessionMinutes;

    private ExerciseLocation exerciseLocation;

    public static AiUserProfileDto of(MemberProfile profile,
                                      BodyComposition composition,
                                      WorkoutEnvironment environment,
                                      int weeklyFrequency,
                                      List<DayOfWeek> effectiveDays,
                                      Integer sessionMinutes) {
        return new AiUserProfileDto(profile.getGender(),
                                    profile.getAge(),
                                    profile.getHeight(),
                                    composition.getWeight(),
                                    composition.getSkeletalMuscleMass(),
                                    composition.getBodyFatPercentage(),
                                    composition.getBodyFatMass(),
                                    profile.getExerciseLevel(),
                                    profile.getCurrentExerciseStatus(),
                                    profile.getPrimaryGoal(),
                                    profile.getGoalStrategy(),
                                    weeklyFrequency,
                                    effectiveDays,
                                    sessionMinutes,
                                    environment.getLocationType());
    }
}
