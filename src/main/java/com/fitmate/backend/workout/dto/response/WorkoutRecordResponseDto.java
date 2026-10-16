package com.fitmate.backend.workout.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.routine.domain.RoutineExercise;
import com.fitmate.backend.workout.domain.CalculatedDifficulty;
import com.fitmate.backend.workout.domain.PerceivedDifficulty;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import com.fitmate.backend.workout.domain.WorkoutRecord;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonPropertyOrder({
        "workoutRecordId",
        "dailyRoutineId",
        "startedAt",
        "endedAt",
        "completionRate",
        "actualExerciseSeconds",
        "actualRestSeconds",
        "minHeartRate",
        "maxHeartRate",
        "perceivedDifficulty",
        "hasNextRoutine",
        "exercises"
})
@Getter
@Builder
public class WorkoutRecordResponseDto {

    private Long workoutRecordId;
    private Long dailyRoutineId;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    // 운동별 수행률을 평균 낸 뒤 반올림한 정수 백분율
    private Integer completionRate;

    // 모든 시간은 초 단위로 반환
    private Integer actualExerciseSeconds;
    private Integer actualRestSeconds;

    // 미측정 시 null. 두 값이 모두 있으면 프론트에서 심박수를 표시
    private Integer minHeartRate;
    private Integer maxHeartRate;

    private PerceivedDifficulty perceivedDifficulty;

    // 같은 주간 루틴에 아직 시작하지 않은 이후 날짜의 루틴이 있는지 여부
    private boolean hasNextRoutine;

    private List<ExerciseDto> exercises;

    public static WorkoutRecordResponseDto of(
            WorkoutRecord workoutRecord,
            int completionRate,
            boolean hasNextRoutine,
            List<ExerciseDto> exercises
    ) {
        return WorkoutRecordResponseDto.builder()
                .workoutRecordId(workoutRecord.getId())
                .dailyRoutineId(workoutRecord.getDailyRoutine().getId())
                .startedAt(workoutRecord.getStartedAt())
                .endedAt(workoutRecord.getEndedAt())
                .completionRate(completionRate)
                .actualExerciseSeconds(workoutRecord.getActualExerciseSeconds())
                .actualRestSeconds(workoutRecord.getActualRestSeconds())
                .minHeartRate(workoutRecord.getMinHeartRate())
                .maxHeartRate(workoutRecord.getMaxHeartRate())
                .perceivedDifficulty(workoutRecord.getPerceivedDifficulty())
                .hasNextRoutine(hasNextRoutine)
                .exercises(exercises)
                .build();
    }

    @JsonPropertyOrder({
            "routineExerciseId",
            "exerciseId",
            "exerciseOrder",
            "exerciseName",
            "baselineRecordType",
            "weight",
            "reps",
            "setCount",
            "durationSeconds",
            "completedSetCount",
            "completedDurationSeconds",
            "calculatedDifficulty"
    })
    @Getter
    @Builder
    public static class ExerciseDto {

        private Long routineExerciseId;
        private Long exerciseId;
        private Integer exerciseOrder;
        private String exerciseName;

        // 프론트에서 세트 기반 운동과 시간 기반 운동의 표시를 구분
        private ExerciseBaselineType baselineRecordType;

        // 루틴에 설정된 목표량
        private BigDecimal weight;
        private Integer reps;
        private Integer setCount;
        private Integer durationSeconds;

        // 실제 수행량. 목표를 초과해도 실제 값은 그대로 반환
        private Integer completedSetCount;
        private Integer completedDurationSeconds;

        private CalculatedDifficulty calculatedDifficulty;

        public static ExerciseDto from(WorkoutExerciseRecord exerciseRecord) {
            RoutineExercise routineExercise = exerciseRecord.getRoutineExercise();

            return ExerciseDto.builder()
                    .routineExerciseId(routineExercise.getId())
                    .exerciseId(routineExercise.getExercise().getId())
                    .exerciseOrder(routineExercise.getExerciseOrder())
                    .exerciseName(routineExercise.getExercise().getNameKo())
                    .baselineRecordType(
                            routineExercise.getExercise().getBaselineRecordType()
                    )
                    .weight(routineExercise.getWeight())
                    .reps(routineExercise.getReps())
                    .setCount(routineExercise.getSetCount())
                    .durationSeconds(routineExercise.getDurationSeconds())
                    .completedSetCount(exerciseRecord.getCompletedSetCount())
                    .completedDurationSeconds(
                            exerciseRecord.getCompletedDurationSeconds()
                    )
                    .calculatedDifficulty(exerciseRecord.getCalculatedDifficulty())
                    .build();
        }
    }
}