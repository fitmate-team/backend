package com.fitmate.backend.member.dto.response;

import com.fitmate.backend.member.domain.*;
import com.fitmate.backend.member.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public class MemberResponseDto {
    // Member
    private Long id;
    private String loginId;

    // MemberProfile
    private Gender gender;
    private Integer age;
    private Double height;
    private ExerciseLevel exerciseLevel;
    private CurrentExerciseStatus currentExerciseStatus;
    private PrimaryGoal primaryGoal;
    private GoalStrategy goalStrategy;
    private Integer weeklyFrequency;
    private Integer sessionMinutes;
    private ExerciseLocation exerciseLocation;
    private Set<DayOfWeek> availableDays;
    private Set<BodyArea> avoidBodyAreas;
    private Double skeletalMuscleMass;
    private Double bodyFatPercentage;
    private Double bodyFatMass;

    // BodyWeight
    private Double weight;

    public static MemberResponseDto from(
            Member member,
            MemberProfile profile,
            BodyWeight bodyWeight
    ) {
        return new MemberResponseDto(
                member.getId(),
                member.getLoginId(),

                profile.getGender(),
                profile.getAge(),
                profile.getHeight(),
                profile.getExerciseLevel(),
                profile.getCurrentExerciseStatus(),
                profile.getPrimaryGoal(),
                profile.getGoalStrategy(),
                profile.getWeeklyFrequency(),
                profile.getSessionMinutes(),
                profile.getExerciseLocation(),
                new HashSet<>(profile.getAvailableDays()), // LazyInitializationException 방지
                new HashSet<>(profile.getAvoidBodyAreas()),
                profile.getSkeletalMuscleMass(),
                profile.getBodyFatPercentage(),
                profile.getBodyFatMass(),

                bodyWeight.getWeight()

        );
    }
}
