package com.fitmate.backend.routine.domain;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.global.common.BaseEntity;
import com.fitmate.backend.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
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

    @Column(name = "weight", precision = 6, scale = 2)
    private BigDecimal weight;

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
            BigDecimal weight,
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
            BigDecimal weight,
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