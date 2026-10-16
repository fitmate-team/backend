package com.fitmate.backend.routine.repository;

import com.fitmate.backend.routine.domain.DailyRoutine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 같은 주간 루틴에서 기준 루틴 날짜보다 뒤에 있는 미시작 루틴을 확인합니다.
     * 운동 기록이 존재하면 종료 여부와 관계없이 이미 시작한 루틴으로 판단합니다.
     */
    @Query("""
        select count(dailyRoutine) > 0
        from DailyRoutine dailyRoutine
        where dailyRoutine.weeklyRoutine.id = :weeklyRoutineId
          and dailyRoutine.routineDate > :routineDate
          and not exists (
              select record.id
              from WorkoutRecord record
              where record.dailyRoutine = dailyRoutine
          )
        """)
    boolean existsNextUnstartedRoutine(
            @Param("weeklyRoutineId") Long weeklyRoutineId,
            @Param("routineDate") LocalDate routineDate
    );
}