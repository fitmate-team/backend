package com.fitmate.backend.routine.repository;

import com.fitmate.backend.routine.domain.ExerciseRoutine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRoutineRepository extends JpaRepository<ExerciseRoutine, Long> {

    /**
     * 일일 루틴에 포함된 운동을 순서대로 조회
     */
    @EntityGraph(attributePaths = {"exercise"})
    List<ExerciseRoutine> findByDailyRoutine_IdOrderByExerciseOrderAsc(
            Long dailyRoutineId
    );
}