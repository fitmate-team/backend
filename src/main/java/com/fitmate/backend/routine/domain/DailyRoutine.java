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
@Table(
        name = "daily_routine",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_routine_0",
                columnNames = {"weekly_routine_id", "routine_date"}
        )
)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "main_target", nullable = false)
    private MainTarget mainTarget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_environment_id", nullable = false)
    private WorkoutEnvironment workoutEnvironment;

    @Column(name = "estimated_duration_minutes", nullable = false)
    private Integer estimatedDurationMinutes;

    @Builder
    public DailyRoutine(WeeklyRoutine weeklyRoutine,
                        LocalDate routineDate,
                        MainTarget mainTarget,
                        WorkoutEnvironment workoutEnvironment,
                        Integer estimatedDurationMinutes) {
        this.weeklyRoutine = weeklyRoutine;
        this.routineDate = routineDate;
        this.mainTarget = mainTarget;
        this.workoutEnvironment = workoutEnvironment;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    /**
     * 오늘의 운동 환경 변경
     *
     * @param workoutEnvironment 변경할 운동 환경
     */
    public void updateWorkoutEnvironment(
            WorkoutEnvironment workoutEnvironment
    ) {
        this.workoutEnvironment = workoutEnvironment;
    }


    // 당일 루틴 재추천 결과 반영
    public void updateRoutine(MainTarget mainTarget,
                              WorkoutEnvironment workoutEnvironment,
                              Integer estimatedDurationMinutes) {
        this.mainTarget = mainTarget;
        this.workoutEnvironment = workoutEnvironment;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

}