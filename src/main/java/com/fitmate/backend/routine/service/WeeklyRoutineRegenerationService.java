package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.ai.service.HardFilterService;
import com.fitmate.backend.routine.domain.*;
import com.fitmate.backend.routine.dto.request.CustomRoutineSaveRequestDto;
import com.fitmate.backend.routine.dto.request.WeeklyRoutineSaveRequestDto;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineAdjustmentRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.routine.repository.WeeklyRoutineRepository;
import com.fitmate.backend.workout.domain.WorkoutRecord;
import com.fitmate.backend.workout.repository.WorkoutExerciseRecordRepository;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyRoutineRegenerationService {

    private final WeeklyRoutineRepository weeklyRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;
    private final WorkoutRecordRepository workoutRecordRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseRoutineAdjustmentRepository adjustmentRepository;
    private final WorkoutExerciseRecordRepository workoutExerciseRecordRepository;
    private final HardFilterService hardFilterService;

    // AI 자동 재생성한 주간 루틴 저장
    @Transactional
    public void saveAiRegeneratedRoutine(Long memberId, WeeklyRoutineSaveRequestDto requestDto) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 이번주에 저장된 주간 루틴 조회
        WeeklyRoutine weeklyRoutine = weeklyRoutineRepository.findByMember_IdAndWeekStartDate(
                        memberId,
                        weekStartDate)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ROUTINE_NOT_FOUND));

        // 새로 저장할 요일을 추출하고 변경 가능한 날짜인지 검사
        List<DayOfWeek> selectedDays = requestDto.getWeeklyPlan()
                .stream()
                .map(WeeklyRoutineSaveRequestDto.DailyDto::getDay)
                .toList();
        validateSelectedDays(memberId, selectedDays);

        // 지난 날짜나 운동 기록이 있어서 남겨둬야 하는 일일 루틴 조회
        List<DailyRoutine> protectedDailyRoutines = getProtectedDailyRoutines(memberId);

        // 새로 만든 루틴에 사용할 운동 환경 조회
        WorkoutEnvironment environment =
                workoutEnvironmentRepository.findWithEquipmentByIdAndMemberId(
                        requestDto.getEnvironmentId(),
                        memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        // 운동 시간, 운동 후보, 중복 운동, 처방값, 운동 처방 조정값 검사
        validateAutoSaveRequest(memberId,
                                requestDto,
                                environment,
                                weekStartDate,
                                protectedDailyRoutines);

        // 보호 날짜는 남겨두고 나머지 기존 일일 루틴과 운동 목록 삭제
        deleteReplaceableDailyRoutines(weeklyRoutine, protectedDailyRoutines);

        // 새로 선택한 날짜의 일일 루틴과 운동 처방 저장
        for (WeeklyRoutineSaveRequestDto.DailyDto dailyDto : requestDto.getWeeklyPlan()) {
            LocalDate routineDate = weekStartDate.plusDays(dailyDto.getDay().getValue() - 1);
            DailyRoutine dailyRoutine = dailyDto.toDailyRoutine(weeklyRoutine,
                                                                routineDate,
                                                                environment);
            dailyRoutineRepository.save(dailyRoutine);

            for (int i = 0; i < dailyDto.getExercises().size(); i++) {
                WeeklyRoutineSaveRequestDto.ExerciseDto exerciseDto = dailyDto.getExercises()
                        .get(i);
                Exercise exercise =
                        exerciseRepository.findByExerciseCode(exerciseDto.getExerciseCode())
                        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EXERCISE_CODE));

                ExerciseRoutine exerciseRoutine = exerciseDto.toExerciseRoutine(dailyRoutine,
                                                                                exercise,
                                                                                i + 1);
                if (exerciseDto.getAppliedAdjustmentId() != null) {
                    exerciseRoutine.markAppliedAdjustment(exerciseDto.getAppliedAdjustmentId());
                }
                exerciseRoutineRepository.save(exerciseRoutine);
            }
        }

        // 보호 날짜와 새로 선택한 날짜를 합쳐 이번주 요일과 설정 갱신
        Set<DayOfWeek> availableDays = protectedDailyRoutines.stream()
                .map(dailyRoutine -> dailyRoutine.getRoutineDate().getDayOfWeek())
                .collect(Collectors.toSet());
        availableDays.addAll(selectedDays);
        weeklyRoutine.updateRegenerationSettings(environment,
                                                 requestDto.getSessionMinutes(),
                                                 availableDays);
    }

    // 사용자가 직접 만든 주간 루틴 재생성 결과 저장
    @Transactional
    public void saveCustomRegeneratedRoutine(Long memberId,
                                             CustomRoutineSaveRequestDto requestDto) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 이번주에 저장된 주간 루틴 조회
        WeeklyRoutine weeklyRoutine = weeklyRoutineRepository.findByMember_IdAndWeekStartDate(
                        memberId,
                        weekStartDate)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ROUTINE_NOT_FOUND));

        // 새로 저장할 요일을 추출하고 변경 가능한 날짜인지 검사
        List<DayOfWeek> selectedDays = requestDto.getWeeklyPlan()
                .stream()
                .map(CustomRoutineSaveRequestDto.DailyDto::getDay)
                .toList();
        validateSelectedDays(memberId, selectedDays);

        // 보호해야 하는 기존 일일 루틴 조회
        List<DailyRoutine> protectedDailyRoutines = getProtectedDailyRoutines(memberId);

        // 날짜별 운동 환경과 사용자가 선택한 운동 코드, 처방값 검사
        validateCustomSaveRequest(memberId, requestDto);

        // 가장 먼저 선택한 운동일의 환경을 주간 루틴 대표 환경으로 사용
        CustomRoutineSaveRequestDto.DailyDto firstDay = requestDto.getWeeklyPlan()
                .stream()
                .filter(day -> day.getDay() == requestDto.getFirstSelectedDay())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_FIRST_SELECTED_ROUTINE_DAY));
        WorkoutEnvironment firstEnvironment =
                workoutEnvironmentRepository.findWithEquipmentByIdAndMemberId(
                        firstDay.getEnvironmentId(),
                        memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        // 보호 날짜는 남겨두고 나머지 기존 일일 루틴과 운동 목록 삭제
        deleteReplaceableDailyRoutines(weeklyRoutine, protectedDailyRoutines);

        // 새로 선택한 날짜마다 운동 환경을 확인하고 일일 루틴과 운동 처방 저장
        for (CustomRoutineSaveRequestDto.DailyDto dailyDto : requestDto.getWeeklyPlan()) {
            WorkoutEnvironment dailyEnvironment =
                    workoutEnvironmentRepository.findWithEquipmentByIdAndMemberId(
                            dailyDto.getEnvironmentId(),
                            memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));
            LocalDate routineDate = weekStartDate.plusDays(dailyDto.getDay().getValue() - 1);
            DailyRoutine dailyRoutine = dailyDto.toDailyRoutine(weeklyRoutine,
                                                                routineDate,
                                                                dailyEnvironment);
            dailyRoutineRepository.save(dailyRoutine);

            for (int i = 0; i < dailyDto.getExercises().size(); i++) {
                CustomRoutineSaveRequestDto.ExerciseDto exerciseDto = dailyDto.getExercises()
                        .get(i);
                Exercise exercise =
                        exerciseRepository.findByExerciseCode(exerciseDto.getExerciseCode())
                        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EXERCISE_CODE));
                exerciseRoutineRepository.save(exerciseDto.toExerciseRoutine(dailyRoutine,
                                                                             exercise,
                                                                             i + 1));
            }
        }

        // 보호 날짜와 새 날짜를 합치고 운동 시간은 기존 설정 그대로 유지
        Set<DayOfWeek> availableDays = protectedDailyRoutines.stream()
                .map(dailyRoutine -> dailyRoutine.getRoutineDate().getDayOfWeek())
                .collect(Collectors.toSet());
        availableDays.addAll(selectedDays);
        weeklyRoutine.updateRegenerationSettings(firstEnvironment,
                                                 weeklyRoutine.getSessionMinutes(),
                                                 availableDays);
    }

    // 기존 운동 기록 확인하고 절대 변경하면 안 되는 DailyRoutine 조회
    public List<DailyRoutine> getProtectedDailyRoutines(Long memberId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 오늘 날짜 기반 이번주 월요일 찾기
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 이번주에 저장된 주간 루틴 조회
        WeeklyRoutine weeklyRoutine = weeklyRoutineRepository.findByMember_IdAndWeekStartDate(
                        memberId,
                        weekStartDate)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ROUTINE_NOT_FOUND));

        // 이번주 주간 루틴에 연결된 일일 루틴 전부 조회
        List<DailyRoutine> dailyRoutines =
                dailyRoutineRepository.findByWeeklyRoutine_IdOrderByRoutineDateAsc(
                weeklyRoutine.getId());

        // DailyRoutine 아이디 추출
        List<Long> dailyRoutineIds = dailyRoutines.stream().map(DailyRoutine::getId).toList();

        // 일일 루틴 아이디로 실제 운동 기록 조회 (없을 수 있으니 삼항 연산자 처리)
        List<WorkoutRecord> workoutRecords = dailyRoutineIds.isEmpty() ? List.of() :
                workoutRecordRepository.findByDailyRoutine_IdIn(
                dailyRoutineIds);

        // 운동 진행중인지 검사 -> 진행중이면 예외
        if (workoutRecords.stream().anyMatch(workoutRecord -> !workoutRecord.isFinished())) {
            throw new CustomException(ErrorCode.WORKOUT_ALREADY_STARTED);
        }

        // 운동 기록이 있는 일일 루틴들의 아이디 수집
        Set<Long> recordedDailyRoutineIds = workoutRecords.stream()
                .map(workoutRecord -> workoutRecord.getDailyRoutine().getId())
                .collect(Collectors.toSet());

        // DailyRoutine 조건 검사 (운동 날짜 오늘보다 이전? || 해당 날짜에 운동 기록 존재?)
        List<DailyRoutine> protectedDailyRoutines = dailyRoutines.stream()
                .filter(d -> d.getRoutineDate().isBefore(today) ||
                        recordedDailyRoutineIds.contains(d.getId()))
                .toList();

        // 보존해야 할 일일 루틴 엔티티 목록
        return protectedDailyRoutines;
    }

    // 프론트가 재생성 대상으로 선택한 요일 검증
    public void validateSelectedDays(Long memberId, List<DayOfWeek> selectedDays) {

        // 선택한 요일이 없으면 예외
        if (selectedDays == null || selectedDays.isEmpty()) {
            throw new CustomException(ErrorCode.NO_AVAILABLE_ROUTINE_DAY);
        }

        // null인 요일이 포함되어 있으면 예외
        if (selectedDays.contains(null)) {
            throw new CustomException(ErrorCode.INVALID_WEEKLY_ROUTINE_DAY);
        }

        // 중복된 요일이 있는지 검사
        Set<DayOfWeek> uniqueDays = new HashSet<>(selectedDays);
        if (uniqueDays.size() != selectedDays.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_WEEKLY_ROUTINE_DAY);
        }

        // 변경 가능한 요일이 맞는지 확인
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        // 보호해야 하는 루틴 목록 가져옴
        List<DailyRoutine> protectedDailyRoutines = getProtectedDailyRoutines(memberId);
        // 보호 일일 루틴에서 요일만 추출함 (ex. Monday, Wednesday)
        Set<DayOfWeek> protectedDays = protectedDailyRoutines.stream()
                .map(e -> e.getRoutineDate().getDayOfWeek())
                .collect(Collectors.toSet());

        // 변경 가능 요일 담을 셋
        Set<DayOfWeek> editableDays = new HashSet<>();

        // 월-일 순회
        // 오늘이거나 미래 요일이어야 함 && 보호 대상 요일에는 포함되어서는 안됨
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.getValue() >= today.getDayOfWeek().getValue() && !protectedDays.contains(day)) {
                editableDays.add(day);
            }
        }

        // 모든 요일 검사 후, 변경 가능한 요일 없으면 예외
        if (editableDays.isEmpty()) {
            throw new CustomException(ErrorCode.NO_AVAILABLE_ROUTINE_DAY);
        }

        // 프론트가 선택한 요일이 변경 가능한 요일에 전부 포함되는지 검사
        if (!editableDays.containsAll(selectedDays)) {
            throw new CustomException(ErrorCode.ROUTINE_REGENERATION_DAY_PROTECTED);
        }
    }

    // 자동 재생성 저장 전에 운동 후보, 중복 운동, 처방값 검사
    private void validateAutoSaveRequest(Long memberId,
                                         WeeklyRoutineSaveRequestDto requestDto,
                                         WorkoutEnvironment environment,
                                         LocalDate weekStartDate,
                                         List<DailyRoutine> protectedDailyRoutines) {

        // 운동 장소, 기구, 수준, 회피 부위, 제외 운동, 전주 수행 운동 조건에 맞는 후보 조회
        Set<String> eligibleExerciseCodes = hardFilterService.getEligibleExercises(memberId,
                                                                                   environment.getId(),
                                                                                   weekStartDate)
                .stream()
                .map(Exercise::getExerciseCode)
                .collect(Collectors.toSet());
        if (eligibleExerciseCodes.isEmpty()) {
            throw new CustomException(ErrorCode.NO_ELIGIBLE_EXERCISE);
        }

        // 이번주 실제 수행한 운동의 주동근 조회 (유산소 제외)
        Set<PrimaryMuscle> performedPrimaryMuscles =
                workoutExerciseRecordRepository.findByWorkoutRecord_DailyRoutine_WeeklyRoutine_Member_IdAndWorkoutRecord_DailyRoutine_RoutineDateBetween(
                        memberId,
                        weekStartDate,
                        weekStartDate.plusDays(6))
                .stream()
                .filter(record -> record.getWorkoutRecord().isFinished())
                .filter(record -> (record.getCompletedSetCount() != null &&
                        record.getCompletedSetCount() > 0) ||
                        (record.getCompletedDurationSeconds() != null &&
                                record.getCompletedDurationSeconds() > 0))
                .map(record -> record.getExerciseRoutine().getExercise().getPrimaryMuscle())
                .filter(muscle -> muscle != PrimaryMuscle.CARDIO)
                .collect(Collectors.toSet());

        // 보호 날짜에 이미 들어간 비유산소 운동 코드를 수집해서 중복 검사에 사용
        Set<String> usedNonCardioCodes = protectedDailyRoutines.stream()
                .flatMap(dailyRoutine -> exerciseRoutineRepository.findByDailyRoutine_IdOrderByExerciseOrderAsc(
                        dailyRoutine.getId()).stream())
                .map(ExerciseRoutine::getExercise)
                .filter(exercise -> exercise.getExerciseType() != ExerciseType.CARDIO)
                .map(Exercise::getExerciseCode)
                .collect(Collectors.toSet());

        Map<String, Long> appliedIdsByExerciseCode = new HashMap<>();

        for (WeeklyRoutineSaveRequestDto.DailyDto dailyDto : requestDto.getWeeklyPlan()) {
            // 하루 운동 시간이 설정 시간을 넘으면 예외
            if (dailyDto.getEstimatedMinutes() == null || dailyDto.getEstimatedMinutes() <= 0 ||
                    requestDto.getSessionMinutes() == null || requestDto.getSessionMinutes() <= 0 ||
                    dailyDto.getEstimatedMinutes() > requestDto.getSessionMinutes()) {
                throw new CustomException(ErrorCode.ROUTINE_DURATION_EXCEEDED);
            }

            // 주간 루틴 자동 생성에서 허용하지 않는 대표 운동 부위 검사
            if (dailyDto.getMainTarget() == MainTarget.CORE) {
                throw new CustomException(ErrorCode.INVALID_WEEKLY_ROUTINE_PRESCRIPTION);
            }

            // 운동 목록이 없으면 예외
            if (dailyDto.getExercises() == null || dailyDto.getExercises().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_WEEKLY_ROUTINE_PRESCRIPTION);
            }

            for (WeeklyRoutineSaveRequestDto.ExerciseDto exerciseDto : dailyDto.getExercises()) {
                Exercise exercise =
                        exerciseRepository.findByExerciseCode(exerciseDto.getExerciseCode())
                        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EXERCISE_CODE));

                // 기본 후보에 없거나 이번주 이미 운동한 부위에 속하면 저장 불가
                if (!eligibleExerciseCodes.contains(exercise.getExerciseCode()) ||
                        performedPrimaryMuscles.contains(exercise.getPrimaryMuscle())) {
                    throw new CustomException(ErrorCode.INVALID_ROUTINE_REGENERATION_EXERCISE);
                }

                // 보호 날짜에 있거나 새로 저장할 다른 날짜와 중복된 비유산소 운동 제외
                if (exercise.getExerciseType() != ExerciseType.CARDIO &&
                        !usedNonCardioCodes.add(exercise.getExerciseCode())) {
                    throw new CustomException(ErrorCode.DUPLICATE_WEEKLY_EXERCISE);
                }

                // 운동 종류에 맞는 세트, 횟수, 중량, 시간, 휴식값인지 검사
                validateExercisePrescription(exercise,
                                             exerciseDto.getWeightKg(),
                                             exerciseDto.getReps(),
                                             exerciseDto.getSets(),
                                             exerciseDto.getDurationSeconds(),
                                             exerciseDto.getRestSeconds(),
                                             ErrorCode.INVALID_WEEKLY_ROUTINE_PRESCRIPTION);

                // 같은 운동 코드의 조정값 ID는 모든 날짜에서 같아야 함
                String exerciseCode = exerciseDto.getExerciseCode();
                Long adjustmentId = exerciseDto.getAppliedAdjustmentId();
                if (appliedIdsByExerciseCode.containsKey(exerciseCode) &&
                        !Objects.equals(appliedIdsByExerciseCode.get(exerciseCode), adjustmentId)) {
                    throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
                }
                appliedIdsByExerciseCode.put(exerciseCode, adjustmentId);

                // 운동 처방 조정값이 있으면 회원, 운동 종목, 처방값이 모두 일치하는지 검사
                if (adjustmentId != null) {
                    ExerciseRoutineAdjustment adjustment = adjustmentRepository.findById(
                                    adjustmentId)
                            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT));
                    boolean validAdjustment =
                            Objects.equals(adjustment.getMember().getId(), memberId) &&
                                    Objects.equals(adjustment.getExercise().getId(),
                                                   exercise.getId()) &&
                                    Objects.equals(adjustment.getWeight(),
                                                   exerciseDto.getWeightKg()) &&
                                    Objects.equals(adjustment.getReps(), exerciseDto.getReps()) &&
                                    Objects.equals(adjustment.getSetCount(),
                                                   exerciseDto.getSets()) && Objects.equals(
                                    adjustment.getDurationSeconds(),
                                    exerciseDto.getDurationSeconds());
                    if (!validAdjustment) {
                        throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
                    }
                }
            }
        }
    }

    // 직접 만든 루틴 저장 전에 날짜별 환경과 운동 처방 검사
    private void validateCustomSaveRequest(Long memberId, CustomRoutineSaveRequestDto requestDto) {
        // 처음 선택한 요일이 새로 저장할 요일 목록에 들어 있는지 검사
        if (requestDto.getFirstSelectedDay() == null || requestDto.getWeeklyPlan()
                .stream()
                .noneMatch(day -> day.getDay() == requestDto.getFirstSelectedDay())) {
            throw new CustomException(ErrorCode.INVALID_FIRST_SELECTED_ROUTINE_DAY);
        }

        for (CustomRoutineSaveRequestDto.DailyDto dailyDto : requestDto.getWeeklyPlan()) {
            // 날짜별 운동 환경 조회 및 회원 소유권 검사
            WorkoutEnvironment environment =
                    workoutEnvironmentRepository.findWithEquipmentByIdAndMemberId(
                            dailyDto.getEnvironmentId(),
                            memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

            // 예상 운동 시간이 양수이고 운동 목록이 있는지 검사 (설정 시간 초과는 허용)
            if (dailyDto.getEstimatedMinutes() == null || dailyDto.getEstimatedMinutes() <= 0 ||
                    dailyDto.getExercises() == null || dailyDto.getExercises().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_CUSTOM_ROUTINE_PRESCRIPTION);
            }

            for (CustomRoutineSaveRequestDto.ExerciseDto exerciseDto : dailyDto.getExercises()) {
                Exercise exercise =
                        exerciseRepository.findByExerciseCode(exerciseDto.getExerciseCode())
                        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EXERCISE_CODE));

                // 선택한 운동이 해당 날짜의 장소와 기구에 맞는지 검사
                if (!exercise.getAvailableLocations().contains(environment.getLocationType()) ||
                        !environment.getEquipment().containsAll(exercise.getEquipment())) {
                    throw new CustomException(ErrorCode.EXERCISE_NOT_AVAILABLE_IN_ENVIRONMENT);
                }

                // 운동 종류에 맞는 세트, 횟수, 중량, 시간, 휴식값인지 검사
                validateExercisePrescription(exercise,
                                             exerciseDto.getWeightKg(),
                                             exerciseDto.getReps(),
                                             exerciseDto.getSets(),
                                             exerciseDto.getDurationSeconds(),
                                             exerciseDto.getRestSeconds(),
                                             ErrorCode.INVALID_CUSTOM_ROUTINE_PRESCRIPTION);
            }
        }
    }

    // 운동 기록 방식에 맞는 처방인지 검사
    private void validateExercisePrescription(Exercise exercise,
                                              Integer weightKg,
                                              Integer reps,
                                              Integer sets,
                                              Integer durationSeconds,
                                              Integer restSeconds,
                                              ErrorCode errorCode) {
        // 세트, 횟수, 중량, 시간, 휴식값의 기본 범위 검사
        if (sets == null || sets <= 0 || restSeconds == null || restSeconds < 0 ||
                (weightKg != null && weightKg <= 0) || (reps != null && reps <= 0) ||
                (durationSeconds != null && durationSeconds <= 0) ||
                exercise.getBaselineRecordType() == null || exercise.getExerciseType() == null) {
            throw new CustomException(errorCode);
        }

        // 중량 운동, 맨몸 운동, 시간 기반 운동의 처방 형식 검사
        boolean invalidPrescription = switch (exercise.getBaselineRecordType()) {
            case WEIGHT_REPS_SETS -> reps == null || durationSeconds != null ||
                    exercise.getExerciseType() == ExerciseType.CARDIO;
            case REPS_SETS -> reps == null || weightKg != null || durationSeconds != null ||
                    exercise.getExerciseType() == ExerciseType.CARDIO;
            case DURATION ->
                    exercise.getExerciseType() != ExerciseType.CARDIO || durationSeconds == null ||
                            reps != null || weightKg != null || sets != 1 || restSeconds != 0;
        };
        if (invalidPrescription) {
            throw new CustomException(errorCode);
        }
    }

    // 보호 날짜를 제외하고 기존 일일 루틴과 운동 목록 삭제
    private void deleteReplaceableDailyRoutines(WeeklyRoutine weeklyRoutine,
                                                List<DailyRoutine> protectedDailyRoutines) {
        Set<Long> protectedDailyRoutineIds = protectedDailyRoutines.stream()
                .map(DailyRoutine::getId)
                .collect(Collectors.toSet());

        // 이번주 기존 루틴 중 보호 대상이 아닌 날짜만 골라냄
        List<DailyRoutine> replaceableDailyRoutines =
                dailyRoutineRepository.findByWeeklyRoutine_IdOrderByRoutineDateAsc(
                        weeklyRoutine.getId())
                .stream()
                .filter(dailyRoutine -> !protectedDailyRoutineIds.contains(dailyRoutine.getId()))
                .toList();

        // 삭제 직전 운동 기록이 생겼는지 한 번 더 확인
        for (DailyRoutine dailyRoutine : replaceableDailyRoutines) {
            if (workoutRecordRepository.existsByDailyRoutine_Id(dailyRoutine.getId())) {
                throw new CustomException(ErrorCode.WORKOUT_ALREADY_STARTED);
            }
        }

        // 기존 운동 목록을 먼저 삭제한 다음 일일 루틴 삭제
        for (DailyRoutine dailyRoutine : replaceableDailyRoutines) {
            exerciseRoutineRepository.deleteAllByDailyRoutineId(dailyRoutine.getId());
        }
        exerciseRoutineRepository.flush();
        dailyRoutineRepository.deleteAll(replaceableDailyRoutines);
        dailyRoutineRepository.flush();
    }
}
