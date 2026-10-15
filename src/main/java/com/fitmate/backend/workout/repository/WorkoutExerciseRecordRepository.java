package com.fitmate.backend.workout.repository;

import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutExerciseRecordRepository extends JpaRepository<WorkoutExerciseRecord, Long> {
}
