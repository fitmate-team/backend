package com.fitmate.backend.workout.repository;

import com.fitmate.backend.workout.domain.WorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecord, Long> {

    boolean existsByDailyRoutine_Id(Long dailyRoutineId);
    Optional<WorkoutRecord> findByDailyRoutine_Id(Long dailyRoutineId);

    @Query("""
    select record
    from WorkoutRecord record
    join fetch record.dailyRoutine dailyRoutine
    join fetch dailyRoutine.weeklyRoutine weeklyRoutine
    where record.id = :workoutRecordId
      and weeklyRoutine.member.id = :memberId
      and record.isFinished = true
    """)
    Optional<WorkoutRecord> findFinishedRecordByIdAndMemberId(
            @Param("workoutRecordId") Long workoutRecordId,
            @Param("memberId") Long memberId
    );

    @Query("""
    select record
    from WorkoutRecord record
    join fetch record.dailyRoutine dailyRoutine
    join fetch dailyRoutine.weeklyRoutine weeklyRoutine
    where weeklyRoutine.member.id = :memberId
      and record.isFinished = true
      and record.startedAt >= :startOfMonth
      and record.startedAt < :startOfNextMonth
    order by record.startedAt desc, record.id desc
    """)
    List<WorkoutRecord> findFinishedRecordsByMemberIdAndStartedAtRange(
            @Param("memberId") Long memberId,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("startOfNextMonth") LocalDateTime startOfNextMonth
    );

    /**
     * 이번 주 완료한 운동 횟수를 조회합니다.
     * 주 시작 일시는 포함하고 다음 주 시작 일시는 제외합니다.
     */
    @Query("""
        select count(record)
        from WorkoutRecord record
        join record.dailyRoutine dailyRoutine
        join dailyRoutine.weeklyRoutine weeklyRoutine
        where weeklyRoutine.member.id = :memberId
          and record.isFinished = true
          and record.startedAt >= :weekStart
          and record.startedAt < :nextWeekStart
        """)
    long countFinishedWorkoutsByMemberIdAndStartedAtRange(
            @Param("memberId") Long memberId,
            @Param("weekStart") LocalDateTime weekStart,
            @Param("nextWeekStart") LocalDateTime nextWeekStart
    );

    /**
     * 일일 루틴 ID 목록에 해당하는 운동 기록을 조회합니다.
     * 시작만 한 기록과 완료한 기록을 모두 포함합니다.
     */
    List<WorkoutRecord> findByDailyRoutine_IdIn(
            List<Long> dailyRoutineIds
    );
}