package com.fitmate.backend.routine.ai.dto.custom.prescription;

import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import com.fitmate.backend.workout.domain.WorkoutRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AiCustomPrescriptionRecordDto {
    private LocalDateTime performedAt;
    private BigDecimal plannedWeightKg;
    private Integer plannedReps;
    private Integer plannedSets;
    private Integer plannedDurationSeconds;
    private Integer plannedRestSeconds;
    private Integer completedSetCount;
    private Integer completedDurationSeconds;

    public static AiCustomPrescriptionRecordDto from(WorkoutExerciseRecord record) {
        return new AiCustomPrescriptionRecordDto(record.getWorkoutRecord().getEndedAt(),
                                                 record.getExerciseRoutine().getWeight(),
                                                 record.getExerciseRoutine().getReps(),
                                                 record.getExerciseRoutine().getSetCount(),
                                                 record.getExerciseRoutine().getDurationSeconds(),
                                                 record.getExerciseRoutine().getRestSeconds(),
                                                 record.getCompletedSetCount(),
                                                 record.getCompletedDurationSeconds());
    }

}
