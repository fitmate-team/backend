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
}