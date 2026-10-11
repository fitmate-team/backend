package com.fitmate.backend.workout.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.RoutineExercise;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.RoutineExerciseRepository;
import com.fitmate.backend.workout.dto.response.TodayWorkoutEnvironmentResponseDto;
import com.fitmate.backend.workout.dto.response.TodayWorkoutResponseDto;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.workout.dto.response.WorkoutLocationValidateResponseDto;

import java.util.Set;
import java.util.stream.Collectors;

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
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;

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
     * 오늘의 운동 환경 선택 화면 조회
     * - 오늘 예정된 운동 환경 ID 조회
     * - 사용자가 등록한 전체 운동 환경 목록 조회
     *
     * @param memberId 로그인한 회원 ID
     * @return 오늘 선택된 운동 환경 ID 및 등록된 운동 환경 목록
     */
    public TodayWorkoutEnvironmentResponseDto getTodayWorkoutEnvironments(
            Long memberId
    ) {
        LocalDate today = LocalDate.now(KOREA_ZONE);

        // 1. 회원의 오늘 운동 루틴 조회
        DailyRoutine dailyRoutine = dailyRoutineRepository
                .findByWeeklyRoutine_Member_IdAndRoutineDate(memberId, today)
                .orElseThrow(() -> new IllegalArgumentException(
                        "오늘의 운동 루틴이 존재하지 않습니다."
                ));

        // 2. 오늘 운동 루틴에 설정된 운동 환경 ID 조회
        Long selectedEnvironmentId =
                dailyRoutine.getWorkoutEnvironment().getId();

        // 3. 회원이 등록한 전체 운동 환경 조회 및 DTO 변환
        List<TodayWorkoutEnvironmentResponseDto.EnvironmentDto> environments =
                workoutEnvironmentRepository.findAllByMemberId(memberId)
                        .stream()
                        .map(TodayWorkoutEnvironmentResponseDto.EnvironmentDto::from)
                        .toList();

        // 4. 오늘 선택된 운동 환경과 등록된 운동 환경 목록 반환
        return TodayWorkoutEnvironmentResponseDto.of(
                selectedEnvironmentId,
                environments
        );
    }

    /**
     * 오늘의 운동 장소 변경 시 루틴 재추천 필요 여부 검사
     * - 현재 루틴의 운동 환경 이름 조회
     * - 새롭게 선택한 운동 환경의 보유 기구 조회
     * - 무산소 운동은 기구 호환성 검사
     * - 유산소 운동은 장소 및 기구 호환성 검사
     *
     * @param memberId 로그인한 회원 ID
     * @param workoutEnvironmentId 새롭게 선택한 운동 환경 ID
     * @return 기존 운동 환경 이름 및 루틴 재추천 필요 여부
     */
    public WorkoutLocationValidateResponseDto validateWorkoutLocation(
            Long memberId,
            Long workoutEnvironmentId
    ) {
        LocalDate today = LocalDate.now(KOREA_ZONE);

        // 1. 오늘의 운동 루틴 조회
        DailyRoutine dailyRoutine = dailyRoutineRepository
                .findByWeeklyRoutine_Member_IdAndRoutineDate(memberId, today)
                .orElseThrow(() -> new IllegalArgumentException(
                        "오늘의 운동 루틴이 존재하지 않습니다."
                ));

        // 2. 현재 루틴의 기준 운동 환경 이름 조회
        String currentRoutineLocationName =
                getLocationName(dailyRoutine.getWorkoutEnvironment());

        // 3. 사용자가 새롭게 선택한 운동 환경 조회
        WorkoutEnvironment selectedEnvironment = workoutEnvironmentRepository
                .findWithEquipmentByIdAndMemberId(workoutEnvironmentId, memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "운동 환경이 존재하지 않거나 접근 권한이 없습니다."
                ));

        // 4. 오늘의 운동 목록 조회
        List<RoutineExercise> routineExercises = routineExerciseRepository
                .findByDailyRoutine_IdOrderByExerciseOrderAsc(dailyRoutine.getId());

        // 5. 선택한 운동 환경에서 사용할 수 있는 기구 코드 조회
        Set<String> availableEquipmentCodes = selectedEnvironment
                .getEquipment()
                .stream()
                .map(Equipment::getEquipmentCode)
                .collect(Collectors.toSet());

        // 6. 오늘의 운동 중 하나라도 수행 불가능하면 재추천 필요
        boolean requiresRerecommendation = routineExercises.stream()
                .anyMatch(routineExercise ->
                        !isExerciseCompatible(
                                routineExercise.getExercise(),
                                selectedEnvironment,
                                availableEquipmentCodes
                        )
                );

        // 7. 검사 결과 반환
        return WorkoutLocationValidateResponseDto.of(
                workoutEnvironmentId,
                currentRoutineLocationName,
                requiresRerecommendation
        );
    }

    /**
     * 선택한 운동 환경에서 개별 운동을 수행할 수 있는지 검사
     * - 무산소: 필요한 운동 기구를 모두 보유하면 수행 가능
     * - 유산소: 운동 기구와 운동 장소 조건을 모두 만족해야 수행 가능
     *
     * @param exercise 검사할 운동
     * @param environment 선택한 운동 환경
     * @param availableEquipmentCodes 해당 환경의 보유 기구 코드
     * @return 운동 수행 가능 여부
     */
    private boolean isExerciseCompatible(
            Exercise exercise,
            WorkoutEnvironment environment,
            Set<String> availableEquipmentCodes
    ) {
        // 1. 운동에 필요한 기구를 모두 보유하고 있는지 검사
        boolean equipmentCompatible = exercise.getEquipment()
                .stream()
                .map(Equipment::getEquipmentCode)
                .allMatch(availableEquipmentCodes::contains);

        if (!equipmentCompatible) {
            return false;
        }

        // 2. 유산소 운동인 경우 운동 장소까지 검사
        if (exercise.getExerciseType() == ExerciseType.CARDIO) {
            return exercise.getAvailableLocations()
                    .contains(environment.getLocationType());
        }

        // 3. 무산소 운동은 기구 조건만 만족하면 수행 가능
        return true;
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