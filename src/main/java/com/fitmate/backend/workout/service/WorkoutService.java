package com.fitmate.backend.workout.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.workout.domain.CalculatedDifficulty;
import com.fitmate.backend.workout.domain.PerceivedDifficulty;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import com.fitmate.backend.workout.domain.WorkoutRecord;
import com.fitmate.backend.workout.dto.request.WorkoutFinishRequestDto;
import com.fitmate.backend.workout.dto.response.*;
import com.fitmate.backend.workout.dto.request.WorkoutRecordCreateRequestDto;
import com.fitmate.backend.workout.repository.WorkoutExerciseRecordRepository;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.routine.repository.ExerciseRoutineAdjustmentRepository;

import java.time.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final WorkoutRecordRepository workoutRecordRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final WorkoutExerciseRecordRepository workoutExerciseRecordRepository;
    private final ExerciseRoutineAdjustmentRepository exerciseRoutineAdjustmentRepository;

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
                .orElseThrow(() -> new CustomException(ErrorCode.TODAY_ROUTINE_NOT_FOUND));

        // 3. 오늘 루틴에 포함된 운동 목록 조회
        List<ExerciseRoutine> exerciseRoutines =
                exerciseRoutineRepository
                        .findByDailyRoutine_IdOrderByExerciseOrderAsc(
                                dailyRoutine.getId()
                        );

        // 4. 운동 목록을 응답 DTO로 변환
        List<TodayWorkoutResponseDto.ExerciseDto> exercises =
                exerciseRoutines.stream()
                        .map(TodayWorkoutResponseDto.ExerciseDto::from)
                        .toList();

        // 5. 평균 휴식 시간 계산
        int averageRestSeconds = calculateAverageRestSeconds(exerciseRoutines);

        // 6. 운동 장소 조회
        WorkoutEnvironment environment = dailyRoutine.getWorkoutEnvironment();

        // 7. 운동 완료 여부 조회
        boolean hasWorkedOut =
                workoutRecordRepository.existsByDailyRoutine_Id(dailyRoutine.getId());

        // 8. 최종 응답 생성
        return TodayWorkoutResponseDto.of(
                dailyRoutine,
                getLocationName(environment),
                averageRestSeconds,
                hasWorkedOut,
                exercises
        );
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
                .orElseThrow(() -> new CustomException(ErrorCode.TODAY_ROUTINE_NOT_FOUND));

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
                .orElseThrow(() -> new CustomException(ErrorCode.TODAY_ROUTINE_NOT_FOUND));

        // 2. 현재 루틴의 기준 운동 환경 이름 조회
        String currentRoutineLocationName =
                getLocationName(dailyRoutine.getWorkoutEnvironment());

        // 3. 사용자가 새롭게 선택한 운동 환경 조회
        WorkoutEnvironment selectedEnvironment = workoutEnvironmentRepository
                .findWithEquipmentByIdAndMemberId(workoutEnvironmentId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        // 4. 오늘의 운동 목록 조회
        List<ExerciseRoutine> exerciseRoutines = exerciseRoutineRepository
                .findByDailyRoutine_IdOrderByExerciseOrderAsc(dailyRoutine.getId());

        // 5. 선택한 운동 환경에서 사용할 수 있는 기구 코드 조회
        Set<String> availableEquipmentCodes = selectedEnvironment
                .getEquipment()
                .stream()
                .map(Equipment::getEquipmentCode)
                .collect(Collectors.toSet());

        // 6. 오늘의 운동 중 하나라도 수행 불가능하면 재추천 필요
        boolean requiresRerecommendation = exerciseRoutines.stream()
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
     * 운동 시작 및 운동 기록 생성
     */
    @Transactional
    public WorkoutStartResponseDto startWorkout(
            Long memberId,
            WorkoutRecordCreateRequestDto request
    ) {
        LocalDate today = LocalDate.now(KOREA_ZONE);

        // 로그인한 회원의 오늘 루틴 조회 및 요청한 루틴 ID 확인
        DailyRoutine dailyRoutine = dailyRoutineRepository
                .findByWeeklyRoutine_Member_IdAndRoutineDate(memberId, today)
                .filter(routine -> routine.getId().equals(request.getDailyRoutineId()))
                .orElseThrow(() ->
                        new CustomException(ErrorCode.TODAY_ROUTINE_NOT_FOUND)
                );

        // 이미 시작한 운동인지 확인
        if (workoutRecordRepository.existsByDailyRoutine_Id(dailyRoutine.getId())) {
            throw new CustomException(ErrorCode.WORKOUT_ALREADY_STARTED);
        }

        // 서버 시각으로 운동 시작 기록 생성
        WorkoutRecord workoutRecord = WorkoutRecord.of(
                dailyRoutine,
                LocalDateTime.now(KOREA_ZONE)
        );

        WorkoutRecord savedRecord = workoutRecordRepository.save(workoutRecord);

        return WorkoutStartResponseDto.of(savedRecord.getId());
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

        // 2. 운동 장소 검증
        return exercise.getAvailableLocations()
                .contains(environment.getLocationType());
    }

    /**
     * 오늘의 운동 환경 변경
     *
     * @param memberId 로그인한 회원 ID
     * @param workoutEnvironmentId 변경할 운동 환경 ID
     */
    @Transactional
    public void changeWorkoutLocation(
            Long memberId,
            Long workoutEnvironmentId
    ) {
        // 1. 오늘의 운동 루틴 조회
        LocalDate today = LocalDate.now(KOREA_ZONE);

        DailyRoutine dailyRoutine = dailyRoutineRepository
                .findByWeeklyRoutine_Member_IdAndRoutineDate(memberId, today)
                .orElseThrow(() -> new CustomException(ErrorCode.TODAY_ROUTINE_NOT_FOUND));

        // 이미 운동을 시작했으면 환경 변경 불가
        if (workoutRecordRepository.existsByDailyRoutine_Id(dailyRoutine.getId())) {
            throw new CustomException(ErrorCode.WORKOUT_ALREADY_STARTED);
        }

        // 2. 변경할 운동 환경 조회 및 소유권 확인
        WorkoutEnvironment selectedEnvironment = workoutEnvironmentRepository
                .findByIdAndMemberId(workoutEnvironmentId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        // 3. 현재 루틴의 운동 목록 조회
        List<ExerciseRoutine> exerciseRoutines = exerciseRoutineRepository
                .findByDailyRoutine_IdOrderByExerciseOrderAsc(dailyRoutine.getId());

        // 4. 선택한 운동 환경의 기구 목록 조회
        Set<String> availableEquipmentCodes = selectedEnvironment
                .getEquipment()
                .stream()
                .map(Equipment::getEquipmentCode)
                .collect(Collectors.toSet());

        // 5. 현재 루틴을 새로운 환경에서 수행할 수 있는지 검사
        boolean incompatible = exerciseRoutines.stream()
                .anyMatch(routineExercise ->
                        !isExerciseCompatible(
                                routineExercise.getExercise(),
                                selectedEnvironment,
                                availableEquipmentCodes
                        )
                );

        if (incompatible) {
            throw new CustomException(ErrorCode.WORKOUT_LOCATION_RERECOMMENDATION_REQUIRED);
        }

        // 6. 운동 환경 변경
        updateWorkoutEnvironment(dailyRoutine, selectedEnvironment);
    }

    /**
     * 정상 종료 또는 중도 종료 결과를 저장합니다.
     *
     * 미수행 운동까지 포함하여 운동별 기록을 생성하고,
     * 하루 운동 기록에 종료 시각, 수행 시간, 심박수, 체감 난이도를 반영합니다.
     */
    @Transactional
    public void finishWorkout(
            Long memberId,
            WorkoutFinishRequestDto request
    ) {
        // 1. 로그인한 회원의 운동 기록 조회
        // 다른 회원의 기록은 조회 실패로 처리합니다.
        WorkoutRecord workoutRecord = workoutRecordRepository
                .findById(request.getWorkoutRecordId())
                .filter(record -> record.getDailyRoutine()
                        .getWeeklyRoutine()
                        .getMember()
                        .getId()
                        .equals(memberId))
                .orElseThrow(() ->
                        new CustomException(ErrorCode.WORKOUT_RECORD_NOT_FOUND)
                );

        DailyRoutine dailyRoutine = workoutRecord.getDailyRoutine();

        // 2. 요청한 루틴과 운동 기록에 연결된 루틴이 같은지 확인
        if (!dailyRoutine.getId().equals(request.getDailyRoutineId())) {
            throw new CustomException(ErrorCode.WORKOUT_ROUTINE_MISMATCH);
        }

        // 3. 이미 종료한 운동은 다시 저장할 수 없습니다.
        if (workoutRecord.isFinished()) {
            throw new CustomException(ErrorCode.WORKOUT_ALREADY_FINISHED);
        }

        // 4. 종료 시각은 시작 시각보다 이전일 수 없습니다.
        if (request.getEndedAt().isBefore(workoutRecord.getStartedAt())) {
            throw new CustomException(ErrorCode.INVALID_WORKOUT_END_TIME);
        }

        // 5. 해당 루틴의 전체 운동을 조회하고 ID를 기준으로 매핑합니다.
        List<ExerciseRoutine> exerciseRoutines = exerciseRoutineRepository
                .findByDailyRoutine_IdOrderByExerciseOrderAsc(dailyRoutine.getId());

        Map<Long, ExerciseRoutine> routineExerciseMap = exerciseRoutines.stream()
                .collect(Collectors.toMap(
                        ExerciseRoutine::getId,
                        Function.identity()
                ));

        // 6. 요청 목록에 동일한 루틴 운동이 중복되었는지 확인
        Set<Long> requestedExerciseIds = request.getExercises().stream()
                .map(WorkoutFinishRequestDto.ExerciseDto::getRoutineExerciseId)
                .collect(Collectors.toSet());

        if (requestedExerciseIds.size() != request.getExercises().size()) {
            throw new CustomException(ErrorCode.DUPLICATE_WORKOUT_EXERCISE);
        }

        // 7. 미수행 운동까지 포함하여 해당 루틴의 모든 운동이 전달되어야 합니다.
        // ID 집합을 비교하여 누락된 운동과 다른 루틴의 운동을 함께 검사합니다.
        if (!requestedExerciseIds.equals(routineExerciseMap.keySet())) {
            throw new CustomException(ErrorCode.WORKOUT_EXERCISE_MISMATCH);
        }

        // 8. 나이별 심박수 계산에 사용할 회원 프로필을 한 번만 조회합니다.
        MemberProfile memberProfile = memberProfileRepository
                .findByMemberId(memberId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.MEMBER_PROFILE_NOT_FOUND)
                );

        List<WorkoutExerciseRecord> exerciseRecords = new ArrayList<>();

        Integer minHeartRate = null;
        Integer maxHeartRate = null;

        // 9. 운동별 수행 기록 생성 및 난이도 계산
        for (WorkoutFinishRequestDto.ExerciseDto exerciseDto
                : request.getExercises()) {

            ExerciseRoutine exerciseRoutine =
                    routineExerciseMap.get(exerciseDto.getRoutineExerciseId());

            Integer exerciseMinHeartRate = exerciseDto.getMinHeartRate();
            Integer exerciseMaxHeartRate = exerciseDto.getMaxHeartRate();

            // 심박수의 양수 여부는 DTO에서 검증합니다.
            // 여기서는 두 값이 모두 있을 때 최소·최대 관계를 확인합니다.
            if (exerciseMinHeartRate != null
                    && exerciseMaxHeartRate != null
                    && exerciseMinHeartRate > exerciseMaxHeartRate) {
                throw new CustomException(ErrorCode.INVALID_HEART_RATE_RANGE);
            }

            // 운동 기준 유형에 따라 목표량과 실제 수행량의 단위를 맞춥니다.
            ExerciseBaselineType baselineType =
                    exerciseRoutine.getExercise().getBaselineRecordType();

            int plannedAmount;
            int completedAmount;

            switch (baselineType) {
                case WEIGHT_REPS_SETS, REPS_SETS -> {
                    // 세트 기반 운동: 목표 세트 수 대비 완료 세트 수
                    plannedAmount = exerciseRoutine.getSetCount();
                    completedAmount = exerciseDto.getCompletedSetCount();
                }

                case DURATION -> {
                    // 시간 기반 운동: 목표 시간 대비 실제 수행 시간
                    // 목표 시간과 실제 수행 시간 모두 초 단위를 사용합니다.
                    plannedAmount = exerciseRoutine.getDurationSeconds();
                    completedAmount = exerciseDto.getCompletedDurationSeconds();
                }

                default -> throw new CustomException(
                        ErrorCode.INVALID_EXERCISE_BASELINE
                );
            }

            // 수행률, 운동별 최대 심박수, 전체 체감 난이도를 합산하여 판정합니다.
            // 미수행은 calculateDifficulty()에서 UNMEASURABLE로 처리합니다.
            CalculatedDifficulty difficulty = calculateDifficulty(
                    plannedAmount,
                    completedAmount,
                    exerciseMaxHeartRate,
                    memberProfile.getAge(),
                    request.getPerceivedDifficulty()
            );

            // 운동 종료 시 해당 운동의 기존 조정값을 삭제합니다.
            // EASY·HARD 운동은 사용자가 새 추천값을 적용하면 다시 저장합니다.
            exerciseRoutineAdjustmentRepository.deleteByMember_IdAndExercise_Id(
                    memberId,
                    exerciseRoutine.getExercise().getId()
            );

            // 운동별 심박수는 난이도 계산에 사용하고,
            // 운동별 DB 기록에는 수행량과 계산된 난이도를 저장합니다.
            WorkoutExerciseRecord exerciseRecord =
                    WorkoutExerciseRecord.builder()
                            .workoutRecord(workoutRecord)
                            .exerciseRoutine(exerciseRoutine)
                            .completedSetCount(exerciseDto.getCompletedSetCount())
                            .completedDurationSeconds(
                                    exerciseDto.getCompletedDurationSeconds()
                            )
                            .calculatedDifficulty(difficulty)
                            .build();

            exerciseRecords.add(exerciseRecord);

            // 운동별 심박수에서 하루 전체 최솟값과 최댓값을 집계합니다.
            // 미측정 값은 제외하며, 모두 미측정이면 null을 유지합니다.
            if (exerciseMinHeartRate != null) {
                minHeartRate = minHeartRate == null
                        ? exerciseMinHeartRate
                        : Math.min(minHeartRate, exerciseMinHeartRate);
            }

            if (exerciseMaxHeartRate != null) {
                maxHeartRate = maxHeartRate == null
                        ? exerciseMaxHeartRate
                        : Math.max(maxHeartRate, exerciseMaxHeartRate);
            }
        }

        // 10. 미수행 운동을 포함한 전체 운동별 기록 저장
        workoutExerciseRecordRepository.saveAll(exerciseRecords);

        // 11. 하루 운동 기록에 종료 결과 반영
        // 영속 상태의 엔티티이므로 변경 감지로 UPDATE됩니다.
        workoutRecord.finish(
                request.getEndedAt(),
                request.getActualExerciseSeconds(),
                request.getActualRestSeconds(),
                minHeartRate,
                maxHeartRate,
                request.getPerceivedDifficulty()
        );
    }

    public WorkoutRecordResponseDto getWorkoutRecord(
            Long memberId,
            Long workoutRecordId
    ) {
        WorkoutRecord workoutRecord = workoutRecordRepository
                .findFinishedRecordByIdAndMemberId(
                        workoutRecordId,
                        memberId
                )
                .orElseThrow(() ->
                        new CustomException(ErrorCode.WORKOUT_RECORD_NOT_FOUND)
                );

        DailyRoutine dailyRoutine = workoutRecord.getDailyRoutine();

        List<WorkoutExerciseRecord> exerciseRecords =
                workoutExerciseRecordRepository
                        .findByWorkoutRecord_IdOrderByExerciseRoutine_ExerciseOrderAsc(
                                workoutRecord.getId()
                        );

        int completionRate = calculateOverallCompletionRate(exerciseRecords);

        boolean hasNextRoutine = dailyRoutineRepository.existsNextUnstartedRoutine(
                dailyRoutine.getWeeklyRoutine().getId(),
                dailyRoutine.getRoutineDate()
        );

        List<WorkoutRecordResponseDto.ExerciseDto> exercises =
                exerciseRecords.stream()
                        .map(WorkoutRecordResponseDto.ExerciseDto::from)
                        .toList();

        return WorkoutRecordResponseDto.of(
                workoutRecord,
                completionRate,
                hasNextRoutine,
                exercises
        );
    }

    public MonthlyWorkoutRecordResponseDto getMonthlyWorkoutRecords(
            Long memberId,
            int year,
            int month
    ) {
        if (year < 1 || year > 9999) {
            throw new CustomException(ErrorCode.INVALID_WORKOUT_YEAR_MONTH);
        }

        YearMonth yearMonth;

        try {
            yearMonth = YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new CustomException(ErrorCode.INVALID_WORKOUT_YEAR_MONTH);
        }

        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime startOfNextMonth = yearMonth.plusMonths(1)
                .atDay(1)
                .atStartOfDay();

        List<WorkoutRecord> workoutRecords = workoutRecordRepository
                .findFinishedRecordsByMemberIdAndStartedAtRange(
                        memberId,
                        startOfMonth,
                        startOfNextMonth
                );

        if (workoutRecords.isEmpty()) {
            return MonthlyWorkoutRecordResponseDto.of(
                    year,
                    month,
                    List.of()
            );
        }

        List<Long> workoutRecordIds = workoutRecords.stream()
                .map(WorkoutRecord::getId)
                .toList();

        Map<Long, List<WorkoutExerciseRecord>> exerciseRecordsByWorkoutId =
                workoutExerciseRecordRepository
                        .findAllByWorkoutRecordIds(workoutRecordIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                exerciseRecord ->
                                        exerciseRecord.getWorkoutRecord().getId()
                        ));

        List<MonthlyWorkoutRecordResponseDto.RecordDto> records =
                workoutRecords.stream()
                        .map(workoutRecord -> {
                            List<WorkoutExerciseRecord> exerciseRecords =
                                    exerciseRecordsByWorkoutId.getOrDefault(
                                            workoutRecord.getId(),
                                            List.of()
                                    );

                            List<String> exerciseNames = exerciseRecords.stream()
                                    .filter(this::hasPerformedExercise)
                                    .map(exerciseRecord ->
                                            exerciseRecord.getExerciseRoutine()
                                                    .getExercise()
                                                    .getNameKo()
                                    )
                                    .toList();

                            int completionRate =
                                    calculateOverallCompletionRate(exerciseRecords);

                            Integer actualExerciseSeconds =
                                    workoutRecord.getActualExerciseSeconds();

                            return MonthlyWorkoutRecordResponseDto.RecordDto.of(
                                    workoutRecord.getId(),
                                    workoutRecord.getStartedAt().toLocalDate(),
                                    workoutRecord.getDailyRoutine()
                                            .getMainTarget()
                                            .getDisplayTitle(),
                                    actualExerciseSeconds == null
                                            ? 0
                                            : actualExerciseSeconds,
                                    completionRate,
                                    workoutRecord.getMaxHeartRate(),
                                    exerciseNames
                            );
                        })
                        .toList();

        return MonthlyWorkoutRecordResponseDto.of(
                year,
                month,
                records
        );
    }

    private boolean hasPerformedExercise(
            WorkoutExerciseRecord exerciseRecord
    ) {
        ExerciseBaselineType baselineType = exerciseRecord.getExerciseRoutine()
                .getExercise()
                .getBaselineRecordType();

        if (baselineType == null) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_BASELINE);
        }

        return switch (baselineType) {
            case WEIGHT_REPS_SETS, REPS_SETS ->
                    exerciseRecord.getCompletedSetCount() != null
                            && exerciseRecord.getCompletedSetCount() > 0;

            case DURATION ->
                    exerciseRecord.getCompletedDurationSeconds() != null
                            && exerciseRecord.getCompletedDurationSeconds() > 0;
        };
    }

    /**
     * 운동별 수행률을 동일한 비중으로 평균 냅니다.
     * 개별 수행률은 반올림하지 않고, 최종 평균만 정수 백분율로 반올림합니다.
     */
    private int calculateOverallCompletionRate(
            List<WorkoutExerciseRecord> exerciseRecords
    ) {
        double averageRate = exerciseRecords.stream()
                .mapToDouble(this::calculateExerciseCompletionRate)
                .average()
                .orElse(0.0);

        return (int) Math.round(averageRate * 100.0);
    }

    /**
     * 운동 유형에 맞는 목표량과 실제 수행량으로 0~1 범위의 수행률을 계산합니다.
     * 미수행은 0%, 목표 초과 수행은 최대 100%로 계산합니다.
     */
    private double calculateExerciseCompletionRate(
            WorkoutExerciseRecord exerciseRecord
    ) {
        ExerciseRoutine exerciseRoutine = exerciseRecord.getExerciseRoutine();

        ExerciseBaselineType baselineType =
                exerciseRoutine.getExercise().getBaselineRecordType();

        Integer plannedAmount;
        Integer completedAmount;

        switch (baselineType) {
            case WEIGHT_REPS_SETS, REPS_SETS -> {
                plannedAmount = exerciseRoutine.getSetCount();
                completedAmount = exerciseRecord.getCompletedSetCount();
            }

            case DURATION -> {
                plannedAmount = exerciseRoutine.getDurationSeconds();
                completedAmount = exerciseRecord.getCompletedDurationSeconds();
            }

            default -> throw new CustomException(
                    ErrorCode.INVALID_EXERCISE_BASELINE
            );
        }

        // 목표량이 올바르지 않으면 나눗셈을 진행하지 않습니다.
        if (plannedAmount == null || plannedAmount <= 0) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_BASELINE);
        }

        if (completedAmount == null || completedAmount <= 0) {
            return 0.0;
        }

        return Math.min(1.0, (double) completedAmount / plannedAmount);
    }

    /**
     * 운동 환경 변경 공통 메서드
     *
     * PATCH: 기존 루틴을 유지하며 운동 환경 변경
     * AI 재추천: 새로운 루틴으로 교체한 후 운동 환경 변경
     *
     * 호출하는 상위 서비스 메서드에서 트랜잭션을 관리해야 합니다.
     */
    public void updateWorkoutEnvironment(
            DailyRoutine dailyRoutine,
            WorkoutEnvironment environment
    ) {
        dailyRoutine.updateWorkoutEnvironment(environment);
    }

    /**
     * 평균 휴식 시간 계산
     */
    private int calculateAverageRestSeconds(
            List<ExerciseRoutine> exerciseRoutines
    ) {

        return (int) Math.round(
                exerciseRoutines.stream()
                        .map(ExerciseRoutine::getRestSeconds)
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
     * 운동별 난이도를 수행률, 최대 심박수, 전체 체감 난이도로 계산합니다.
     *
     * 점수가 낮을수록 어렵게, 높을수록 쉽게 판정합니다.
     *
     * 점수 구성:
     * - 수행률: -10 ~ 0점
     * - 최대 심박수: -3 ~ +3점
     * - 체감 난이도: -1 ~ +1점
     *
     * 최종 판정:
     * - 미수행: UNMEASURABLE
     * - 합산 점수 -3 이하: HARD
     * - 합산 점수 +3 이상: EASY
     * - 나머지: NORMAL
     *
     * 목표량과 실제 수행량은 동일한 단위로 전달해야 합니다.
     * - 세트 기반 운동: 목표 세트 수 / 완료 세트 수
     * - 시간 기반 운동: 목표 시간(초) / 실제 수행 시간(초)
     *
     * 목표량은 양수이며, 나이와 요청값은 사전에 검증되었다고 가정합니다.
     * 점수와 판정 구간은 Fitmate 내부 평가 규칙입니다.
     */
    private CalculatedDifficulty calculateDifficulty(
            int plannedAmount,
            int completedAmount,
            Integer maxHeartRate,
            int age,
            PerceivedDifficulty perceivedDifficulty
    ) {
        // 1. 수행량이 0이면 심박수와 체감 난이도를 반영하지 않고 측정 불가로 처리합니다.
        if (completedAmount == 0) {
            return CalculatedDifficulty.UNMEASURABLE;
        }

        // 2. 심박수가 없으면 수행량과 체감 난이도로 판정합니다.
        if (maxHeartRate == null) {
            // 목표량을 일부만 수행한 운동은 어려움으로 판정합니다.
            if (completedAmount < plannedAmount) {
                return CalculatedDifficulty.HARD;
            }

            // 목표량 이상 수행한 운동은 하루 전체 체감 난이도를 기준으로 판정합니다.
            // 심박수가 없는 운동도 EASY, NORMAL, HARD로 판정될 수 있도록 합니다.
            return switch (perceivedDifficulty) {
                case UNABLE_TO_COMPLETE, NEAR_LIMIT ->
                        CalculatedDifficulty.HARD;
                case APPROPRIATE ->
                        CalculatedDifficulty.NORMAL;
                case SLIGHTLY_EASY, TOO_EASY ->
                        CalculatedDifficulty.EASY;
            };
        }

        // 3. 심박수가 있으면 수행률, 운동별 심박수, 하루 전체 체감 난이도를 점수로 합산합니다.
        // 목표량을 초과해 수행해도 수행률은 100%로 제한하여 추가 가점을 부여하지 않습니다.
        double completionRate = Math.min(
                1.0,
                (double) completedAmount / plannedAmount
        );

        // 수행률에 비례해 감점합니다.
        // 예: 25% → -7.5점, 50% → -5점, 75% → -2.5점, 100% → 0점
        double performanceScore = -10.0 * (1.0 - completionRate);

        // 4. 나이로 추정 최대 심박수를 구하고, 해당 운동에서 측정한 최대 심박수의 비율을 계산합니다.
        double estimatedMaxHeartRate = 220.0 - age;
        double heartRateRatio = maxHeartRate / estimatedMaxHeartRate;

        // 추정 최대 심박수 대비 60%를 0점으로 설정합니다.
        // 비율이 낮을수록 쉬움 방향으로 가점하고, 높을수록 어려움 방향으로 감점합니다.
        // 예: 50% → +2점, 60% → 0점, 70% → -2점
        double rawHeartRateScore = (0.6 - heartRateRatio) * 20.0;

        // 심박수 점수는 -3~+3점으로 제한합니다.
        double heartRateScore = Math.max(
                -3.0,
                Math.min(3.0, rawHeartRateScore)
        );

        // 5. 하루 전체 체감 난이도는 각 운동의 난이도에 -1~+1점의 보정값으로 반영합니다.
        double perceivedScore = switch (perceivedDifficulty) {
            case UNABLE_TO_COMPLETE -> -1.0;
            case NEAR_LIMIT -> -0.5;
            case APPROPRIATE -> 0.0;
            case SLIGHTLY_EASY -> 0.5;
            case TOO_EASY -> 1.0;
        };

        // 6. 수행률 점수, 심박수 점수, 체감 난이도 보정값을 합산합니다.
        double totalScore =
                performanceScore + heartRateScore + perceivedScore;

        // 부동소수점 계산 오차를 고려하여 경계값인 -3점과 +3점의 판정을 보정합니다.
        double epsilon = 1e-9;

        // 총점이 -3점 이하면 어려움으로 판정합니다.
        if (totalScore <= -3.0 + epsilon) {
            return CalculatedDifficulty.HARD;
        }

        // 총점이 +3점 이상이면 쉬움으로 판정합니다.
        if (totalScore >= 3.0 - epsilon) {
            return CalculatedDifficulty.EASY;
        }

        // 두 경계값 사이이면 적절함으로 판정합니다.
        return CalculatedDifficulty.NORMAL;
    }
}