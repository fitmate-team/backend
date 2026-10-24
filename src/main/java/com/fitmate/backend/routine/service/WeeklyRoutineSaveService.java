package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.repository.MemberRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.domain.WeeklyRoutine;
import com.fitmate.backend.routine.dto.request.WeeklyRoutineSaveRequestDto;
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
public class WeeklyRoutineSaveService {
    private final MemberRepository memberRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final WeeklyRoutineRepository weeklyRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;

    @Transactional
    public void save(Long memberId, WeeklyRoutineSaveRequestDto requestDto) {
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
                Integer exerciseOrder = i + 1;
                exerciseRoutineRepository.save(exerciseDto.toExerciseRoutine(dailyRoutine,
                                                                             exercise,
                                                                             exerciseOrder));
            }
        }

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
}
