package com.fitmate.backend.member.domain;

import com.fitmate.backend.exercise.domain.Exercise;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "exercise_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseBaseline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    private Integer weight;

    private Integer reps;

    @Column(name = "set_count")
    private Integer sets;

    private Integer durationSeconds;

    @Builder
    public ExerciseBaseline(Member member,
                            Exercise exercise,
                            Integer weight,
                            Integer reps,
                            Integer sets,
                            Integer durationSeconds) {
        this.member = member;
        this.exercise = exercise;
        this.weight = weight;
        this.reps = reps;
        this.sets = sets;
        this.durationSeconds = durationSeconds;
    }
}
