package com.fitmate.backend.workout.repository;

import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.workout.domain.WorkoutRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutGrowthRepository
        extends Repository<WorkoutRecord, Long> {

    @Query("""
        select year(body.createdAt) as year,
               month(body.createdAt) as month,
               max(body.weight) as maxWeight,
               min(body.weight) as minWeight,
               max(body.skeletalMuscleMass) as maxSkeletalMuscleMass,
               min(body.skeletalMuscleMass) as minSkeletalMuscleMass,
               max(body.bodyFatPercentage) as maxBodyFatPercentage,
               min(body.bodyFatPercentage) as minBodyFatPercentage,
               max(body.bodyFatMass) as maxBodyFatMass,
               min(body.bodyFatMass) as minBodyFatMass
        from BodyComposition body
        where body.member.id = :memberId
          and body.createdAt >= :startAt
          and body.createdAt <= :now
        group by year(body.createdAt), month(body.createdAt)
        order by year(body.createdAt), month(body.createdAt)
        """)
    List<BodyMonthlyProjection> findBodyMonthlyRecords(
            @Param("memberId") Long memberId,
            @Param("startAt") LocalDateTime startAt,
            @Param("now") LocalDateTime now
    );

    @Query("""
        select distinct exercise.id as exerciseId,
                        exercise.nameKo as exerciseName
        from WorkoutExerciseRecord exerciseRecord
        join exerciseRecord.workoutRecord record
        join record.dailyRoutine dailyRoutine
        join dailyRoutine.weeklyRoutine weeklyRoutine
        join exerciseRecord.routineExercise routineExercise
        join routineExercise.exercise exercise
        where weeklyRoutine.member.id = :memberId
          and record.isFinished = true
          and record.startedAt >= :startAt
          and record.startedAt <= :now
          and exercise.baselineRecordType = :baselineType
          and exerciseRecord.completedSetCount > 0
          and routineExercise.weight is not null
        order by exercise.id asc
        """)
    List<ExerciseProjection> findPerformedWeightExercises(
            @Param("memberId") Long memberId,
            @Param("startAt") LocalDateTime startAt,
            @Param("now") LocalDateTime now,
            @Param("baselineType") ExerciseBaselineType baselineType
    );

    @Query("""
        select year(record.startedAt) as year,
               month(record.startedAt) as month,
               max(routineExercise.weight) as maxWeight,
               min(routineExercise.weight) as minWeight
        from WorkoutExerciseRecord exerciseRecord
        join exerciseRecord.workoutRecord record
        join record.dailyRoutine dailyRoutine
        join dailyRoutine.weeklyRoutine weeklyRoutine
        join exerciseRecord.routineExercise routineExercise
        join routineExercise.exercise exercise
        where weeklyRoutine.member.id = :memberId
          and exercise.id = :exerciseId
          and exercise.baselineRecordType = :baselineType
          and record.isFinished = true
          and record.startedAt >= :startAt
          and record.startedAt <= :now
          and exerciseRecord.completedSetCount > 0
          and routineExercise.weight is not null
        group by year(record.startedAt), month(record.startedAt)
        order by year(record.startedAt), month(record.startedAt)
        """)
    List<WeightMonthlyProjection> findWeightMonthlyRecords(
            @Param("memberId") Long memberId,
            @Param("exerciseId") Long exerciseId,
            @Param("startAt") LocalDateTime startAt,
            @Param("now") LocalDateTime now,
            @Param("baselineType") ExerciseBaselineType baselineType
    );

    @Query("""
        select year(dailyRoutine.routineDate) as year,
               month(dailyRoutine.routineDate) as month,
               count(dailyRoutine.id) as scheduledCount,
               count(record.id) as attendedCount
        from DailyRoutine dailyRoutine
        join dailyRoutine.weeklyRoutine weeklyRoutine
        left join WorkoutRecord record
          on record.dailyRoutine = dailyRoutine
         and record.startedAt <= :now
        where weeklyRoutine.member.id = :memberId
          and dailyRoutine.routineDate >= :startDate
          and dailyRoutine.routineDate <= :today
        group by year(dailyRoutine.routineDate),
                 month(dailyRoutine.routineDate)
        order by year(dailyRoutine.routineDate),
                 month(dailyRoutine.routineDate)
        """)
    List<AttendanceMonthlyProjection> findAttendanceMonthlyRecords(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("today") LocalDate today,
            @Param("now") LocalDateTime now
    );

    interface BodyMonthlyProjection {
        Integer getYear();
        Integer getMonth();
        Double getMaxWeight();
        Double getMinWeight();
        Double getMaxSkeletalMuscleMass();
        Double getMinSkeletalMuscleMass();
        Double getMaxBodyFatPercentage();
        Double getMinBodyFatPercentage();
        Double getMaxBodyFatMass();
        Double getMinBodyFatMass();
    }

    interface ExerciseProjection {
        Long getExerciseId();
        String getExerciseName();
    }

    interface WeightMonthlyProjection {
        Integer getYear();
        Integer getMonth();
        BigDecimal getMaxWeight();
        BigDecimal getMinWeight();
    }

    interface AttendanceMonthlyProjection {
        Integer getYear();
        Integer getMonth();
        Long getScheduledCount();
        Long getAttendedCount();
    }
}