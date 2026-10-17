package com.fitmate.backend.workout.repository;

import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutExerciseRecordRepository extends JpaRepository<WorkoutExerciseRecord,
        Long> {
    List<WorkoutExerciseRecord> findByWorkoutRecord_DailyRoutine_WeeklyRoutine_Member_IdAndWorkoutRecord_DailyRoutine_RoutineDateBetween(
            Long memberId,
            LocalDate previousWeekStart,
            LocalDate previousWeekEnd);

    List<WorkoutExerciseRecord> findByWorkoutRecord_IdOrderByRoutineExercise_ExerciseOrderAsc(Long workoutRecordId);

    /**
     * 미수행 운동을 포함한 운동별 기록을 루틴 순서대로 조회합니다.
     * 응답에 필요한 루틴 운동과 운동 정보를 함께 조회합니다.
     */
    @EntityGraph(attributePaths = {"routineExercise", "routineExercise.exercise"})

    @Query("""
            select exerciseRecord
            from WorkoutExerciseRecord exerciseRecord
            join fetch exerciseRecord.workoutRecord workoutRecord
            join fetch exerciseRecord.routineExercise routineExercise
            join fetch routineExercise.exercise exercise
            where workoutRecord.id in :workoutRecordIds
            order by workoutRecord.id asc,
                     routineExercise.exerciseOrder asc,
                     exerciseRecord.id asc
            """)
    List<WorkoutExerciseRecord> findAllByWorkoutRecordIds(@Param("workoutRecordIds") List<Long> workoutRecordIds);
}
