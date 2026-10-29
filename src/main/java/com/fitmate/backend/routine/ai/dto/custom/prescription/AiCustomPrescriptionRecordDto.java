package com.fitmate.backend.routine.ai.dto.custom.prescription;

import com.fitmate.backend.workout.domain.CalculatedDifficulty;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AiCustomPrescriptionRecordDto {

    private LocalDateTime performedAt;
    private Integer plannedWeightKg;
    private Integer plannedReps;
    private Integer plannedSets;
    private Integer plannedDurationSeconds;
    private Integer plannedRestSeconds;
    private Integer completedSetCount;
    private Integer completedDurationSeconds;
    private CalculatedDifficulty calculatedDifficulty;

    public static AiCustomPrescriptionRecordDto from(WorkoutExerciseRecord record) {

        return new AiCustomPrescriptionRecordDto(record.getWorkoutRecord().getEndedAt(),
                                                 record.getExerciseRoutine().getWeight(),
                                                 record.getExerciseRoutine().getReps(),
                                                 record.getExerciseRoutine().getSetCount(),
                                                 record.getExerciseRoutine().getDurationSeconds(),
                                                 record.getExerciseRoutine().getRestSeconds(),
                                                 record.getCompletedSetCount(),
                                                 record.getCompletedDurationSeconds(),
                                                 record.getCalculatedDifficulty());
    }
}
