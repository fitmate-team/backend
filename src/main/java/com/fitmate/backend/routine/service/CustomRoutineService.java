package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.MemberRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.WeeklyRoutine;
import com.fitmate.backend.routine.dto.request.CustomRoutineSaveRequestDto;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.routine.repository.WeeklyRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomRoutineService {
    private final MemberRepository memberRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final WeeklyRoutineRepository weeklyRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final MemberProfileRepository memberProfileRepository;

    @Transactional
    public void saveRoutine(Long memberId, CustomRoutineSaveRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_PROFILE_NOT_FOUND));

        // 이번주 시작일 계산 및 중복 확인
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (weeklyRoutineRepository.existsByMember_IdAndWeekStartDate(memberId, weekStartDate)) {
            throw new CustomException(ErrorCode.DUPLICATE_WEEKLY_ROUTINE);
        }

        validateSaveRequest(memberId, requestDto, today);

        // 운동 첫째날 꺼내기
        CustomRoutineSaveRequestDto.DailyDto firstDay = requestDto.getWeeklyPlan()
                .stream()
                .filter(day -> day.getDay() == requestDto.getFirstSelectedDay())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_FIRST_SELECTED_ROUTINE_DAY));

        // 첫째날의 환경 Id 꺼내기
        WorkoutEnvironment firstEnvironment =
                workoutEnvironmentRepository.findWithEquipmentByIdAndMemberId(
                        firstDay.getEnvironmentId(),
                        memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        // WeeklyRoutine 생성 및 저장
        WeeklyRoutine weeklyRoutine = requestDto.toWeeklyRoutine(member,
                                                                 firstEnvironment,
                                                                 weekStartDate,
                                                                 memberProfile.getSessionMinutes());
        weeklyRoutineRepository.save(weeklyRoutine);

        // 타입 변수명 : 반복할 리스트
        for (CustomRoutineSaveRequestDto.DailyDto dailyDto : requestDto.getWeeklyPlan()) {

            WorkoutEnvironment dailyEnvironment =
                    workoutEnvironmentRepository.findWithEquipmentByIdAndMemberId(
                            dailyDto.getEnvironmentId(),
                            memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

            DayOfWeek day = dailyDto.getDay();
            LocalDate routineDate = weekStartDate.plusDays(day.getValue() - 1); // 은동 예정 날짜
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
                Integer exerciseOrder = i + 1;
                exerciseRoutineRepository.save(exerciseDto.toExerciseRoutine(dailyRoutine,
                                                                             exercise,
                                                                             exerciseOrder));
            }
        }

    }


    private void validateSaveRequest(Long memberId,
                                     CustomRoutineSaveRequestDto requestDto,
                                     LocalDate today) {

        // 최초 선택한 요일이 최종 주간 계획에 포함되는지
        boolean hasFirstSelectedDay = requestDto.getWeeklyPlan()
                .stream()
                .anyMatch(daily -> daily.getDay() == requestDto.getFirstSelectedDay());

        if (!hasFirstSelectedDay) {
            throw new CustomException(ErrorCode.INVALID_FIRST_SELECTED_ROUTINE_DAY);
        }

        Set<DayOfWeek> seenDays = new HashSet<>();

        for (CustomRoutineSaveRequestDto.DailyDto dailyDto : requestDto.getWeeklyPlan()) {

            DayOfWeek day = dailyDto.getDay();

            // 지난 요일 검사
            if (day.getValue() < today.getDayOfWeek().getValue()) {
                throw new CustomException(ErrorCode.INVALID_WEEKLY_ROUTINE_DAY);
            }

            // 중복 요일 검사
            if (!seenDays.add(day)) {
                throw new CustomException(ErrorCode.DUPLICATE_WEEKLY_ROUTINE_DAY);
            }

            // 날짜별 환경 조회 및 회원 소유권 검사
            WorkoutEnvironment environment =
                    workoutEnvironmentRepository.findWithEquipmentByIdAndMemberId(
                            dailyDto.getEnvironmentId(),
                            memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

            for (CustomRoutineSaveRequestDto.ExerciseDto exerciseDto : dailyDto.getExercises()) {

                // 운동 코드 유효성 검사
                Exercise exercise =
                        exerciseRepository.findByExerciseCode(exerciseDto.getExerciseCode())
                        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EXERCISE_CODE));

                // 장소 및 필요 기구 적합성 검사
                if (!exercise.getAvailableLocations().contains(environment.getLocationType()) ||
                        !environment.getEquipment().containsAll(exercise.getEquipment())) {
                    throw new CustomException(ErrorCode.EXERCISE_NOT_AVAILABLE_IN_ENVIRONMENT);
                }

                // 처방 수치의 기본 형식 검사
                if (exerciseDto.getSets() == null || exerciseDto.getSets() <= 0 ||
                        exerciseDto.getRestSeconds() == null || exerciseDto.getRestSeconds() < 0 ||
                        (exerciseDto.getWeightKg() != null &&
                                exerciseDto.getWeightKg().signum() <= 0) ||
                        (exerciseDto.getReps() != null && exerciseDto.getReps() <= 0) ||
                        (exerciseDto.getDurationSeconds() != null &&
                                exerciseDto.getDurationSeconds() <= 0)) {
                    throw new CustomException(ErrorCode.INVALID_CUSTOM_ROUTINE_PRESCRIPTION);
                }

                // 운동 기록 유형별 처방 형식 검사
                boolean invalidPrescription = switch (exercise.getBaselineRecordType()) {

                    case WEIGHT_REPS_SETS -> exerciseDto.getReps() == null ||
                            exerciseDto.getDurationSeconds() != null ||
                            exercise.getExerciseType() == ExerciseType.CARDIO;
                    case REPS_SETS ->
                            exerciseDto.getReps() == null || exerciseDto.getWeightKg() != null ||
                                    exerciseDto.getDurationSeconds() != null ||
                                    exercise.getExerciseType() == ExerciseType.CARDIO;
                    case DURATION -> exercise.getExerciseType() != ExerciseType.CARDIO ||
                            exerciseDto.getDurationSeconds() == null ||
                            exerciseDto.getReps() != null || exerciseDto.getWeightKg() != null ||
                            exerciseDto.getSets() != 1 || exerciseDto.getRestSeconds() != 0;
                };

                if (invalidPrescription) {
                    throw new CustomException(ErrorCode.INVALID_CUSTOM_ROUTINE_PRESCRIPTION);
                }
            }
        }
    }

}
