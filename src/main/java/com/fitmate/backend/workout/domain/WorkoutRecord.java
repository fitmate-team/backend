package com.fitmate.backend.workout.domain;

import com.fitmate.backend.global.common.BaseEntity;
import com.fitmate.backend.routine.domain.DailyRoutine;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workout_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_routine_id", nullable = false, unique = true)
    private DailyRoutine dailyRoutine;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @org.hibernate.annotations.ColumnDefault("false")
    @Column(name = "is_finished", nullable = false)
    private boolean isFinished;

    @Column(name = "actual_exercise_minutes")
    private Integer actualExerciseMinutes;

    @Column(name = "actual_rest_seconds")
    private Integer actualRestSeconds;

    @Column(name = "min_heart_rate")
    private Integer minHeartRate;

    @Column(name = "max_heart_rate")
    private Integer maxHeartRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "perceived_difficulty")
    private PerceivedDifficulty perceivedDifficulty;

    @Builder
    public WorkoutRecord(DailyRoutine dailyRoutine,
                         LocalDateTime startedAt,
                         LocalDateTime endedAt,
                         boolean isFinished,
                         Integer actualExerciseMinutes,
                         Integer actualRestSeconds,
                         Integer minHeartRate,
                         Integer maxHeartRate,
                         PerceivedDifficulty perceivedDifficulty) {
        this.dailyRoutine = dailyRoutine;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.isFinished = isFinished;
        this.actualExerciseMinutes = actualExerciseMinutes;
        this.actualRestSeconds = actualRestSeconds;
        this.minHeartRate = minHeartRate;
        this.maxHeartRate = maxHeartRate;
        this.perceivedDifficulty = perceivedDifficulty;
    }
}