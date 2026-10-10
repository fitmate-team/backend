package com.fitmate.backend.routine.repository;

import com.fitmate.backend.routine.domain.DailyRoutine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyRoutineRepository extends JpaRepository<DailyRoutine, Long> {

    /**
     * 회원 ID와 날짜로 일일 운동 루틴 조회
     */
    @EntityGraph(attributePaths = {"workoutEnvironment"})
    Optional<DailyRoutine> findByWeeklyRoutine_Member_IdAndRoutineDate(
            Long memberId,
            LocalDate routineDate
    );
}