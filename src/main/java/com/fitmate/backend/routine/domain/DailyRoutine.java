package com.fitmate.backend.routine.domain;

import com.fitmate.backend.global.common.BaseEntity;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_routine", uniqueConstraints = {@UniqueConstraint(name = "uk_daily_routine_0", columnNames = {"weekly_routine_id", "routine_date"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRoutine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_routine_id", nullable = false)
    private WeeklyRoutine weeklyRoutine;

    @Column(name = "routine_date", nullable = false)
    private LocalDate routineDate;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_environment_id", nullable = false)
    private WorkoutEnvironment workoutEnvironment;

    @Column(name = "estimated_duration_minutes", nullable = false)
    private Integer estimatedDurationMinutes;

    @Builder
    public DailyRoutine(WeeklyRoutine weeklyRoutine,
                        LocalDate routineDate,
                        String title,
                        WorkoutEnvironment workoutEnvironment,
                        Integer estimatedDurationMinutes) {
        this.weeklyRoutine = weeklyRoutine;
        this.routineDate = routineDate;
        this.title = title;
        this.workoutEnvironment = workoutEnvironment;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }
}