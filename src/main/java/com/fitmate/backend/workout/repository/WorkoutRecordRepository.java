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

    /**
     * 로그인한 회원의 종료된 운동 기록을 시작 일시 범위로 조회합니다.
     * 요청 날짜의 00:00 이상, 다음 날 00:00 미만을 조회합니다.
     */
    @Query("""
        select record
        from WorkoutRecord record
        join fetch record.dailyRoutine dailyRoutine
        join fetch dailyRoutine.weeklyRoutine weeklyRoutine
        where weeklyRoutine.member.id = :memberId
          and record.isFinished = true
          and record.startedAt >= :startOfDay
          and record.startedAt < :startOfNextDay
        """)
    Optional<WorkoutRecord> findFinishedRecordByMemberIdAndStartedAtRange(
            @Param("memberId") Long memberId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay
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
}