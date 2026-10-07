package com.fitmate.backend.member.domain.enums;

import lombok.Getter;

@Getter
public enum GoalStrategy {

    // 체지방 감량
    FAT_LOSS_STRENGTH_FOCUS(PrimaryGoal.FAT_LOSS),
    FAT_LOSS_CARDIO_FOCUS(PrimaryGoal.FAT_LOSS),
    FAT_LOSS_BALANCED(PrimaryGoal.FAT_LOSS),

    // 근육 성장
    MUSCLE_GAIN_HYPERTROPHY_FOCUS(PrimaryGoal.MUSCLE_GAIN),
    MUSCLE_GAIN_BODY_PART_FOCUS(PrimaryGoal.MUSCLE_GAIN),
    MUSCLE_GAIN_FULL_BODY_BALANCED(PrimaryGoal.MUSCLE_GAIN),

    // 근력 향상
    STRENGTH_BIG_THREE_FOCUS(PrimaryGoal.STRENGTH_GAIN),
    STRENGTH_UPPER_BODY_FOCUS(PrimaryGoal.STRENGTH_GAIN),
    STRENGTH_LOWER_BODY_FOCUS(PrimaryGoal.STRENGTH_GAIN),
    STRENGTH_OVERALL(PrimaryGoal.STRENGTH_GAIN),

    // 신체 재구성
    RECOMPOSITION_FAT_LOSS_PRIORITY(PrimaryGoal.BODY_RECOMPOSITION),
    RECOMPOSITION_MUSCLE_GAIN_PRIORITY(PrimaryGoal.BODY_RECOMPOSITION),
    RECOMPOSITION_BALANCED(PrimaryGoal.BODY_RECOMPOSITION),

    // 체력 향상
    FITNESS_CARDIORESPIRATORY_FOCUS(PrimaryGoal.FITNESS_IMPROVEMENT),
    FITNESS_MUSCULAR_ENDURANCE_FOCUS(PrimaryGoal.FITNESS_IMPROVEMENT),
    FITNESS_OVERALL(PrimaryGoal.FITNESS_IMPROVEMENT),

    // 운동 습관 형성
    HABIT_SHORT_AND_FREQUENT(PrimaryGoal.HABIT_FORMATION),
    HABIT_THREE_TIMES_A_WEEK(PrimaryGoal.HABIT_FORMATION),
    HABIT_GRADUAL_INTENSITY(PrimaryGoal.HABIT_FORMATION);

    private final PrimaryGoal primaryGoal;

    GoalStrategy(PrimaryGoal primaryGoal) {
        this.primaryGoal = primaryGoal;
    }

}