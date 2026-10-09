package com.fitmate.backend.routine.domain;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.global.common.BaseEntity;
import com.fitmate.backend.routine.domain.DailyRoutine;
import jakarta.persistence.*;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routine_exercise", uniqueConstraints = {@UniqueConstraint(name =
        "uk_routine_exercise_0", columnNames = {"daily_routine_id", "exercise_order"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineExercise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_routine_id", nullable = false)
    private DailyRoutine dailyRoutine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "exercise_order", nullable = false)
    private Integer exerciseOrder;

    @Column(name = "weight", precision = 6, scale = 2)
    private BigDecimal weight;

    @Column(name = "reps")
    private Integer reps;

    @Column(name = "set_count")
    private Integer setCount;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "rest_seconds", nullable = false)
    private Integer restSeconds;

    @Builder
    public RoutineExercise(DailyRoutine dailyRoutine,
                           Exercise exercise,
                           Integer exerciseOrder,
                           BigDecimal weight,
                           Integer reps,
                           Integer setCount,
                           Integer durationSeconds,
                           Integer restSeconds) {
        this.dailyRoutine = dailyRoutine;
        this.exercise = exercise;
        this.exerciseOrder = exerciseOrder;
        this.weight = weight;
        this.reps = reps;
        this.setCount = setCount;
        this.durationSeconds = durationSeconds;
        this.restSeconds = restSeconds;
    }
}