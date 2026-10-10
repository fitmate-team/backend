package com.fitmate.backend.workout.dto.response;

import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import com.fitmate.backend.routine.domain.MainTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayWorkoutResponseDto {

    // 오늘의 운동 식별자
    private Long dailyRoutineId;

    // 운동 날짜
    private LocalDate workoutDate;

    // 오늘의 운동 장소 식별자
    private Long workoutEnvironmentId;

    // 운동 장소 유형 (HOME, GYM, OUTDOOR)
    private ExerciseLocation locationType;

    // 화면에 표시할 운동 장소 이름
    private String locationName;

    // 메인 운동 부위 제목 (예: 상체 PUSH)
    private String title;

    // 예상 운동 시간 (분)
    private Integer estimatedDurationMinutes;

    // 운동 종목 수
    private Integer exerciseCount;

    // 평균 휴식 시간 (초)
    private Integer averageRestSeconds;

    // 오늘 운동 시작 여부 (WorkoutRecord 존재 여부)
    private Boolean hasWorkedOut;

    // 오늘의 운동 목록
    private List<ExerciseDto> exercises;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseDto {

        // 루틴에 포함된 운동 식별자
        private Long routineExerciseId;

        // 운동 마스터 식별자
        private Long exerciseId;

        // 운동 종류
        private ExerciseType exerciseType;

        // 기록 타입
        private ExerciseBaselineType baselineRecordType;

        // 운동 이름
        private String exerciseName;

        // 운동 순서
        private Integer exerciseOrder;

        // 중량 (kg), 필요 없거나 미설정 시 null
        private Double weightKg;

        // 반복 횟수, 유산소 운동이면 null 가능
        private Integer reps;

        // 세트 수
        private Integer sets;

        // 운동 시간 (초), 시간 기반 운동이면 사용
        private Integer durationSeconds;

        // 세트 간 휴식 시간 (초)
        private Integer restSeconds;

        // 중량 설정 필요 여부
        private Boolean weightRequired;
    }
}