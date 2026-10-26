package com.fitmate.backend.workout.repository;

import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutExerciseRecordRepository extends JpaRepository<WorkoutExerciseRecord,
        Long> {

    List<WorkoutExerciseRecord> findByWorkoutRecord_DailyRoutine_WeeklyRoutine_Member_IdAndWorkoutRecord_DailyRoutine_RoutineDateBetween(
            Long memberId,
            LocalDate previousWeekStart,
            LocalDate previousWeekEnd);

    /**
     * 미수행 운동을 포함한 운동별 기록을 루틴 순서대로 조회합니다.
     * 응답에 필요한 루틴 운동과 운동 정보를 함께 조회합니다.
     */
    @EntityGraph(attributePaths = {"exerciseRoutine", "exerciseRoutine.exercise"})
    List<WorkoutExerciseRecord> findByWorkoutRecord_IdOrderByExerciseRoutine_ExerciseOrderAsc(Long workoutRecordId);

    @Query("""
            select exerciseRecord
            from WorkoutExerciseRecord exerciseRecord
            join fetch exerciseRecord.workoutRecord workoutRecord
            join fetch exerciseRecord.exerciseRoutine exerciseRoutine
            join fetch exerciseRoutine.exercise exercise
            where workoutRecord.id in :workoutRecordIds
            order by workoutRecord.id asc,
                     exerciseRoutine.exerciseOrder asc,
                     exerciseRecord.id asc
            """)
    List<WorkoutExerciseRecord> findAllByWorkoutRecordIds(@Param("workoutRecordIds") List<Long> workoutRecordIds);


    /**
     * 동일 운동의 최근 유효 수행 기록 조회
     * - 운동을 종료한 기록만 조회
     * - 실제 수행량이 0인 기록 제외
     * - 운동 종료 시각 최신순 정렬
     */
    @Query("""
            select exerciseRecord
            from WorkoutExerciseRecord exerciseRecord
            join fetch exerciseRecord.workoutRecord workoutRecord
            join workoutRecord.dailyRoutine dailyRoutine
            join dailyRoutine.weeklyRoutine weeklyRoutine
            join fetch exerciseRecord.exerciseRoutine exerciseRoutine
            join fetch exerciseRoutine.exercise exercise
            where weeklyRoutine.member.id = :memberId
              and exercise.exerciseCode = :exerciseCode
              and workoutRecord.isFinished = true
              and workoutRecord.endedAt is not null
              and (exerciseRecord.completedSetCount > 0
                   or exerciseRecord.completedDurationSeconds > 0)
            order by workoutRecord.endedAt desc,
                     workoutRecord.id desc,
                     exerciseRecord.id desc
            """)
    List<WorkoutExerciseRecord> findRecentPerformedRecordsByMemberIdAndExerciseCode(@Param(
            "memberId") Long memberId,
                                                                                    @Param("exerciseCode") String exerciseCode,
                                                                                    Pageable pageable);

}
