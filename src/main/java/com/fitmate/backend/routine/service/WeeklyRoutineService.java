package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.repository.MemberRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutineAdjustment;
import com.fitmate.backend.routine.domain.WeeklyRoutine;
import com.fitmate.backend.routine.dto.request.WeeklyRoutineSaveRequestDto;
import com.fitmate.backend.routine.dto.response.WeeklyRoutineResponseDto;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineAdjustmentRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.routine.repository.WeeklyRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyRoutineService {
    private final MemberRepository memberRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final WeeklyRoutineRepository weeklyRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final ExerciseRoutineAdjustmentRepository adjustmentRepository;


    @Transactional
    public void saveRoutine(Long memberId, WeeklyRoutineSaveRequestDto requestDto) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        WorkoutEnvironment environment =
                workoutEnvironmentRepository.findByIdAndMemberId(requestDto.getEnvironmentId(),
                                                                                          memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if (weeklyRoutineRepository.existsByMember_IdAndWeekStartDate(memberId, weekStartDate)) {
            throw new CustomException(ErrorCode.DUPLICATE_WEEKLY_ROUTINE);
        }
        validateDaysAndTime(requestDto, today);

        Map<String, Long> appliedIdsByExerciseCode = new HashMap<>();

        WeeklyRoutine weeklyRoutine = requestDto.toWeeklyRoutine(member,
                                                                 environment,
                                                                 weekStartDate);
        weeklyRoutineRepository.save(weeklyRoutine);

        // 타입 변수명 : 반복할 리스트
        for (WeeklyRoutineSaveRequestDto.DailyDto dailyDto : requestDto.getWeeklyPlan()) {
            DayOfWeek day = dailyDto.getDay();
            LocalDate routineDate = weekStartDate.plusDays(day.getValue() - 1); // 은동 예정 날짜
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

                // DB에 저장하기 전에 운동 기록 유형에 맞는 처방인지 검사
                validateExercisePrescription(exerciseDto, exercise);


                Integer exerciseOrder = i + 1;

                // 1. 같은 운동 코드의 모든 날짜에서
                //    조정값 ID가 일관되는지 확인
                String exerciseCode = exerciseDto.getExerciseCode();
                Long requestedAdjustmentId = exerciseDto.getAppliedAdjustmentId();

                if (appliedIdsByExerciseCode.containsKey(exerciseCode) && !Objects.equals(
                        appliedIdsByExerciseCode.get(exerciseCode),
                        requestedAdjustmentId)) {
                    throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
                }

                appliedIdsByExerciseCode.put(exerciseCode, requestedAdjustmentId);

                // 2. 기존 방식대로 운동 루틴 엔티티 생성
                ExerciseRoutine exerciseRoutine = exerciseDto.toExerciseRoutine(dailyRoutine,
                                                                                exercise,
                                                                                exerciseOrder);

                // 3. ⑤ 조정값을 적용한 운동인 경우만 검증
                if (requestedAdjustmentId != null) {

                    ExerciseRoutineAdjustment adjustment = adjustmentRepository.findById(
                                    requestedAdjustmentId)
                            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT));

                    // 회원 ID와 운동 ID가 올바른지,
                    // 현재 대기 조정값과 처방이 일치하는지 확인
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

                    // 4. 검증된 조정값 ID를 운동 루틴에 기록
                    exerciseRoutine.markAppliedAdjustment(adjustment.getId());
                }

// 5. 운동 루틴 저장
                exerciseRoutineRepository.save(exerciseRoutine);

            }
        }

    }

    public WeeklyRoutineResponseDto getRoutine(Long memberId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        WeeklyRoutine weeklyRoutine = weeklyRoutineRepository.findByMember_IdAndWeekStartDate(
                        memberId,
                        weekStartDate)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ROUTINE_NOT_FOUND));

        List<DailyRoutine> dailyRoutines =
                dailyRoutineRepository.findByWeeklyRoutine_IdOrderByRoutineDateAsc(
                weeklyRoutine.getId());

        List<WeeklyRoutineResponseDto.DailyDto> weeklyPlan = new ArrayList<>();

        for (DailyRoutine dailyRoutine : dailyRoutines) {

            List<ExerciseRoutine> exerciseRoutines =
                    exerciseRoutineRepository.findByDailyRoutine_IdOrderByExerciseOrderAsc(
                    dailyRoutine.getId());

            List<WeeklyRoutineResponseDto.ExerciseDto> exercises = exerciseRoutines.stream()
                    .map(WeeklyRoutineResponseDto.ExerciseDto::from)
                    .toList();

            WeeklyRoutineResponseDto.DailyDto dailyPlan = WeeklyRoutineResponseDto.DailyDto.of(
                    dailyRoutine,
                    exercises);

            weeklyPlan.add(dailyPlan);

        }

        return WeeklyRoutineResponseDto.of(weeklyRoutine, weeklyPlan);

    }

    private void validateDaysAndTime(WeeklyRoutineSaveRequestDto requestDto, LocalDate today) {
        Set<DayOfWeek> seenDays = new HashSet<>(); // 이미 등장한 요일

        for (WeeklyRoutineSaveRequestDto.DailyDto dailyDto : requestDto.getWeeklyPlan()) {
            DayOfWeek day = dailyDto.getDay();

            // 지난 요일 검사
            if (day.getValue() < today.getDayOfWeek().getValue()) {
                throw new CustomException(ErrorCode.INVALID_WEEKLY_ROUTINE_DAY);
            }
            // 중복 요일 검사
            if (!seenDays.add(day)) {
                throw new CustomException(ErrorCode.DUPLICATE_WEEKLY_ROUTINE_DAY);
            }
            // 운동시간 초과 검사
            if (requestDto.getSessionMinutes() < dailyDto.getEstimatedMinutes()) {
                throw new CustomException(ErrorCode.ROUTINE_DURATION_EXCEEDED);
            }
        }
    }


    private void validateExercisePrescription(WeeklyRoutineSaveRequestDto.ExerciseDto exerciseDto,
                                              Exercise exercise) {
        // 1. 기본 처방 수치 검사
        if (exerciseDto.getSets() == null || exerciseDto.getSets() <= 0 ||
                exerciseDto.getRestSeconds() == null || exerciseDto.getRestSeconds() < 0 ||
                (exerciseDto.getWeightKg() != null && exerciseDto.getWeightKg() <= 0) ||
                (exerciseDto.getReps() != null && exerciseDto.getReps() <= 0) ||
                (exerciseDto.getDurationSeconds() != null &&
                        exerciseDto.getDurationSeconds() <= 0) ||
                exercise.getBaselineRecordType() == null || exercise.getExerciseType() == null) {
            throw new CustomException(ErrorCode.INVALID_WEEKLY_ROUTINE_PRESCRIPTION);
        }

        // 2. 운동 기록 방식별 처방 검증
        boolean invalidPrescription = switch (exercise.getBaselineRecordType()) {

            case WEIGHT_REPS_SETS ->
                    exerciseDto.getReps() == null || exerciseDto.getDurationSeconds() != null ||
                            exercise.getExerciseType() == ExerciseType.CARDIO;

            case REPS_SETS -> exerciseDto.getReps() == null || exerciseDto.getWeightKg() != null ||
                    exerciseDto.getDurationSeconds() != null ||
                    exercise.getExerciseType() == ExerciseType.CARDIO;

            case DURATION -> exercise.getExerciseType() != ExerciseType.CARDIO ||
                    exerciseDto.getDurationSeconds() == null || exerciseDto.getReps() != null ||
                    exerciseDto.getWeightKg() != null || exerciseDto.getSets() != 1 ||
                    exerciseDto.getRestSeconds() != 0;
        };

        if (invalidPrescription) {
            throw new CustomException(ErrorCode.INVALID_WEEKLY_ROUTINE_PRESCRIPTION);
        }
    }

}
