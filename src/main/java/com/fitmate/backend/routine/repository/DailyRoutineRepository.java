package com.fitmate.backend.routine.repository;

import com.fitmate.backend.routine.domain.DailyRoutine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyRoutineRepository extends JpaRepository<DailyRoutine, Long> {

    /**
     * 회원 ID와 날짜로 일일 운동 루틴 조회
     */
    @EntityGraph(attributePaths = {"workoutEnvironment"})
    Optional<DailyRoutine> findByWeeklyRoutine_Member_IdAndRoutineDate(Long memberId,
                                                                       LocalDate routineDate);

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
    boolean existsNextUnstartedRoutine(@Param("weeklyRoutineId") Long weeklyRoutineId,
                                       @Param("routineDate") LocalDate routineDate);

    List<DailyRoutine> findByWeeklyRoutine_IdOrderByRoutineDateAsc(Long weeklyRoutineId);

    /**
     * 데일리 루틴 ID와 회원 ID로 해당 회원의 데일리 루틴을 조회합니다.
     * 운동 환경과 보유 기구를 함께 조회합니다.
     */
    @EntityGraph(attributePaths = {
            "workoutEnvironment",
            "workoutEnvironment.equipment"
    })
    Optional<DailyRoutine> findByIdAndWeeklyRoutine_Member_Id(
            Long dailyRoutineId,
            Long memberId
    );

    /**
     * 연속 운동 횟수 계산을 위해 오늘까지 예정된 운동을 최신순으로 조회합니다.
     */
    @Query("""
        select dailyRoutine
        from DailyRoutine dailyRoutine
        where dailyRoutine.weeklyRoutine.member.id = :memberId
          and dailyRoutine.routineDate <= :today
        order by dailyRoutine.routineDate desc, dailyRoutine.id desc
        """)
    List<DailyRoutine> findRoutinesForConsecutiveWorkoutCount(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today
    );
}