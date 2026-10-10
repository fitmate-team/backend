package com.fitmate.backend.workout.repository;

import com.fitmate.backend.workout.domain.WorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecord, Long> {

    boolean existsByDailyRoutine_Id(Long dailyRoutineId);
}