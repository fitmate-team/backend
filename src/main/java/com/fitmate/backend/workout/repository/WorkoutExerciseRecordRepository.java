package com.fitmate.backend.workout.repository;

import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutExerciseRecordRepository extends JpaRepository<WorkoutExerciseRecord,
        Long> {
    List<WorkoutExerciseRecord> findByWorkoutRecord_DailyRoutine_WeeklyRoutine_Member_IdAndWorkoutRecord_DailyRoutine_RoutineDateBetween(
            Long memberId,
            LocalDate previousWeekStart,
            LocalDate previousWeekEnd);
}
