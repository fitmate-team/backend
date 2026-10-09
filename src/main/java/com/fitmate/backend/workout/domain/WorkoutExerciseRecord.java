package com.fitmate.backend.workout.domain;

import com.fitmate.backend.global.common.BaseEntity;
import com.fitmate.backend.routine.domain.RoutineExercise;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workout_exercise_record", uniqueConstraints = {@UniqueConstraint(name =
        "uk_workout_exercise_record_0", columnNames = {"workout_record_id",
        "routine_exercise_id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutExerciseRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_record_id", nullable = false)
    private WorkoutRecord workoutRecord;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_exercise_id", nullable = false, unique = true)
    private RoutineExercise routineExercise;

    @org.hibernate.annotations.ColumnDefault("0")
    @Column(name = "completed_set_count", nullable = false)
    private Integer completedSetCount;

    @Column(name = "completed_duration_seconds")
    private Integer completedDurationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculated_difficulty", nullable = false)
    private CalculatedDifficulty calculatedDifficulty;

    @Builder
    public WorkoutExerciseRecord(WorkoutRecord workoutRecord,
                                 RoutineExercise routineExercise,
                                 Integer completedSetCount,
                                 Integer completedDurationSeconds,
                                 CalculatedDifficulty calculatedDifficulty) {
        this.workoutRecord = workoutRecord;
        this.routineExercise = routineExercise;
        this.completedSetCount = completedSetCount != null ? completedSetCount : 0;
        this.completedDurationSeconds =
                completedDurationSeconds != null ? completedDurationSeconds : 0;
        this.calculatedDifficulty = calculatedDifficulty;
    }
}