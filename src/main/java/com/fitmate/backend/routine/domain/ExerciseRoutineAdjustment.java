package com.fitmate.backend.routine.domain;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.global.common.BaseEntity;
import com.fitmate.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "exercise_routine_adjustment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exercise_routine_adjustment_member_exercise",
                        columnNames = {"member_id", "exercise_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseRoutineAdjustment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "reps")
    private Integer reps;

    @Column(name = "set_count", nullable = false)
    private Integer setCount;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Builder
    public ExerciseRoutineAdjustment(
            Member member,
            Exercise exercise,
            Integer weight,
            Integer reps,
            Integer setCount,
            Integer durationSeconds
    ) {
        this.member = member;
        this.exercise = exercise;
        this.weight = weight;
        this.reps = reps;
        this.setCount = setCount;
        this.durationSeconds = durationSeconds;
    }

    public void update(
            Integer weight,
            Integer reps,
            Integer setCount,
            Integer durationSeconds
    ) {
        this.weight = weight;
        this.reps = reps;
        this.setCount = setCount;
        this.durationSeconds = durationSeconds;
    }
}