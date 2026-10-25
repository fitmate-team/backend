package com.fitmate.backend.routine.dto.response;

import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.domain.MainTarget;
import com.fitmate.backend.routine.domain.WeeklyRoutine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class WeeklyRoutineCurrentResponseDto {
    private Long weeklyRoutineId;
    private LocalDate weekStartDate;
    private Integer sessionMinutes;
    private Long workoutEnvironmentId;
    private List<DailyDto> weeklyPlan;

    public static WeeklyRoutineCurrentResponseDto of(WeeklyRoutine weeklyRoutine,
                                                     List<DailyDto> weeklyPlan) {
        return new WeeklyRoutineCurrentResponseDto(weeklyRoutine.getId(),
                                                   weeklyRoutine.getWeekStartDate(),
                                                   weeklyRoutine.getSessionMinutes(),
                                                   weeklyRoutine.getWorkoutEnvironment().getId(),
                                                   weeklyPlan);
    }

    @Getter
    @AllArgsConstructor
    public static class DailyDto {
        private Long dailyRoutineId;
        private LocalDate routineDate;
        private DayOfWeek day;
        private MainTarget mainTarget;
        private Integer estimatedMinutes;
        private List<ExerciseDto> exercises;

        public static DailyDto of(DailyRoutine dailyRoutine, List<ExerciseDto> exercises) {
            return new DailyDto(dailyRoutine.getId(),
                                dailyRoutine.getRoutineDate(),
                                dailyRoutine.getRoutineDate().getDayOfWeek(),
                                dailyRoutine.getMainTarget(),
                                dailyRoutine.getEstimatedDurationMinutes(),
                                exercises);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ExerciseDto {
        private Long routineExerciseId;
        private String exerciseCode;
        private String nameKo;
        private Integer exerciseOrder;
        private BigDecimal weightKg;
        private Integer reps;
        private Integer sets;
        private Integer durationSeconds;
        private Integer restSeconds;

        public static ExerciseDto from(ExerciseRoutine exerciseRoutine) {
            return new ExerciseDto(exerciseRoutine.getId(),
                                   exerciseRoutine.getExercise().getExerciseCode(),
                                   exerciseRoutine.getExercise().getNameKo(),
                                   exerciseRoutine.getExerciseOrder(),
                                   exerciseRoutine.getWeight(),
                                   exerciseRoutine.getReps(),
                                   exerciseRoutine.getSetCount(),
                                   exerciseRoutine.getDurationSeconds(),
                                   exerciseRoutine.getRestSeconds());
        }
    }


}
