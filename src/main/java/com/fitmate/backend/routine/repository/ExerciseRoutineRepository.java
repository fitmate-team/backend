package com.fitmate.backend.routine.repository;

import com.fitmate.backend.routine.domain.ExerciseRoutine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ExerciseRoutineRepository extends JpaRepository<ExerciseRoutine, Long> {

    /**
     * 일일 루틴에 포함된 운동을 순서대로 조회
     */
    @EntityGraph(attributePaths = {"exercise"})
    List<ExerciseRoutine> findByDailyRoutine_IdOrderByExerciseOrderAsc(
            Long dailyRoutineId
    );

    /**
     * 해당 데일리 루틴에 포함된 운동 ID 목록을 조회합니다.
     * 각 운동의 데일리 루틴 포함 여부를 판단하는 데 사용합니다.
     */
    @Query("""
        select distinct exerciseRoutine.exercise.id
        from ExerciseRoutine exerciseRoutine
        where exerciseRoutine.dailyRoutine.id = :dailyRoutineId
        """)
    Set<Long> findExerciseIdsByDailyRoutineId(
            @Param("dailyRoutineId") Long dailyRoutineId
    );
}