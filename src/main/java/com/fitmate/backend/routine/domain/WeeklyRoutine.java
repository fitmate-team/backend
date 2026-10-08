package com.fitmate.backend.routine.domain;

import com.fitmate.backend.global.common.BaseEntity;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weekly_routine", uniqueConstraints = {@UniqueConstraint(name = "uk_weekly_routine_0", columnNames = {"member_id", "week_start_date"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyRoutine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "session_minutes", nullable = false)
    private Integer sessionMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_environment_id", nullable = false)
    private WorkoutEnvironment workoutEnvironment;

    @ElementCollection
    @CollectionTable(name = "weekly_routine_available_days",
            joinColumns = @JoinColumn(name = "weekly_routine_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_weekly_routine_day",
                    columnNames = {"weekly_routine_id", "day_of_week"}))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, columnDefinition = "varchar(255)")
    private Set<DayOfWeek> availableDays = new HashSet<>();

    @Builder
    public WeeklyRoutine(Member member,
                         LocalDate weekStartDate,
                         Integer sessionMinutes,
                         WorkoutEnvironment workoutEnvironment,
                         Set<DayOfWeek> availableDays) {
        this.member = member;
        this.weekStartDate = weekStartDate;
        this.sessionMinutes = sessionMinutes;
        this.workoutEnvironment = workoutEnvironment;
        this.availableDays = availableDays != null ? new HashSet<>(availableDays) : new HashSet<>();
    }
}