package com.fitmate.backend.workout.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.RoutineExercise;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.RoutineExerciseRepository;
import com.fitmate.backend.workout.dto.response.TodayWorkoutResponseDto;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final DailyRoutineRepository dailyRoutineRepository;
    private final RoutineExerciseRepository routineExerciseRepository;
    private final WorkoutRecordRepository workoutRecordRepository;

    /**
     * 오늘의 운동 조회
     *
     * @param memberId 로그인한 회원 ID
     * @return 오늘의 운동 정보
     */
    public TodayWorkoutResponseDto getTodayWorkout(Long memberId) {

        // 1. 오늘 날짜 조회
        LocalDate today = LocalDate.now(KOREA_ZONE);

        // 2. 회원의 오늘 운동 루틴 조회
        DailyRoutine dailyRoutine = dailyRoutineRepository
                .findByWeeklyRoutine_Member_IdAndRoutineDate(memberId, today)
                .orElseThrow(() ->
                        new IllegalArgumentException("오늘의 운동 루틴이 존재하지 않습니다.")
                );

        // 3. 오늘 루틴에 포함된 운동 목록 조회
        List<RoutineExercise> routineExercises =
                routineExerciseRepository
                        .findByDailyRoutine_IdOrderByExerciseOrderAsc(
                                dailyRoutine.getId()
                        );

        // 4. 운동 목록을 응답 DTO로 변환
        List<TodayWorkoutResponseDto.ExerciseDto> exercises =
                routineExercises.stream()
                        .map(this::toExerciseDto)
                        .toList();

        // 5. 평균 휴식 시간 계산
        int averageRestSeconds = calculateAverageRestSeconds(routineExercises);

        // 6. 운동 장소 조회
        WorkoutEnvironment environment = dailyRoutine.getWorkoutEnvironment();

        // 7. 운동 완료 여부 조회
        boolean hasWorkedOut =
                workoutRecordRepository.existsByDailyRoutine_Id(dailyRoutine.getId());

        // 8. 최종 응답 생성
        return TodayWorkoutResponseDto.builder()
                .dailyRoutineId(dailyRoutine.getId())
                .workoutDate(dailyRoutine.getRoutineDate())
                .workoutEnvironmentId(environment.getId())
                .locationType(environment.getLocationType())
                .locationName(getLocationName(environment))
                .title(dailyRoutine.getTitle())
                .estimatedDurationMinutes(dailyRoutine.getEstimatedDurationMinutes())
                .exerciseCount(exercises.size())
                .averageRestSeconds(averageRestSeconds)
                .hasWorkedOut(hasWorkedOut)
                .exercises(exercises)
                .build();
    }

    /**
     * 루틴 운동 엔티티를 응답 DTO로 변환
     */
    /**
     * 루틴 운동 엔티티를 응답 DTO로 변환
     */
    private TodayWorkoutResponseDto.ExerciseDto toExerciseDto(
            RoutineExercise routineExercise
    ) {

        // 운동 마스터 정보
        Exercise exercise = routineExercise.getExercise();

        // 운동 기록 방식
        ExerciseBaselineType baselineRecordType =
                exercise.getBaselineRecordType();

        // 중량 기반 운동인데 추천 중량이 설정되지 않은 경우
        boolean weightRequired =
                baselineRecordType == ExerciseBaselineType.WEIGHT_REPS_SETS
                        && routineExercise.getWeight() == null;

        // 추천 중량 변환
        Double weightKg = convertWeightToDouble(routineExercise.getWeight());

        return TodayWorkoutResponseDto.ExerciseDto.builder()
                // 루틴 운동 식별자
                .routineExerciseId(routineExercise.getId())

                // 운동 마스터 정보
                .exerciseId(exercise.getId())
                .exerciseType(exercise.getExerciseType())
                .baselineRecordType(baselineRecordType)
                .exerciseName(exercise.getNameKo())

                // 오늘의 운동 설정값
                .exerciseOrder(routineExercise.getExerciseOrder())
                .weightKg(weightKg)
                .reps(routineExercise.getReps())
                .sets(routineExercise.getSetCount())
                .durationSeconds(routineExercise.getDurationSeconds())
                .restSeconds(routineExercise.getRestSeconds())

                // 중량 설정 필요 여부
                .weightRequired(weightRequired)
                .build();
    }

    /**
     * 평균 휴식 시간 계산
     */
    private int calculateAverageRestSeconds(
            List<RoutineExercise> routineExercises
    ) {

        return (int) Math.round(
                routineExercises.stream()
                        .map(RoutineExercise::getRestSeconds)
                        .filter(restSeconds -> restSeconds != null)
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0)
        );
    }

    /**
     * 운동 장소 표시 이름 반환
     */
    private String getLocationName(WorkoutEnvironment environment) {

        ExerciseLocation locationType = environment.getLocationType();

        return switch (locationType) {
            case HOME -> "집";
            case GYM -> environment.getGymName();
            case OUTDOOR -> "야외";
        };
    }

    /**
     * 중량 타입 변환
     */
    private Double convertWeightToDouble(BigDecimal weight) {

        if (weight == null) {
            return null;
        }

        return weight.doubleValue();
    }
}