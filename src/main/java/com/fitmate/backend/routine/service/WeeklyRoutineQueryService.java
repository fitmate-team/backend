package com.fitmate.backend.routine.service;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.domain.WeeklyRoutine;
import com.fitmate.backend.routine.dto.response.WeeklyRoutineCurrentResponseDto;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyRoutineQueryService {
    private final WeeklyRoutineRepository weeklyRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;

    public WeeklyRoutineCurrentResponseDto getCurrentWeeklyRoutine(Long memberId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        WeeklyRoutine weeklyRoutine = weeklyRoutineRepository.findByMember_IdAndWeekStartDate(
                        memberId,
                        weekStartDate)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ROUTINE_NOT_FOUND));

        List<DailyRoutine> dailyRoutines =
                dailyRoutineRepository.findByWeeklyRoutine_IdOrderByRoutineDateAsc(
                        weeklyRoutine.getId());

        List<WeeklyRoutineCurrentResponseDto.DailyDto> weeklyPlan = new ArrayList<>();

        for (DailyRoutine dailyRoutine : dailyRoutines) {

            List<ExerciseRoutine> exerciseRoutines =
                    exerciseRoutineRepository.findByDailyRoutine_IdOrderByExerciseOrderAsc(
                            dailyRoutine.getId());

            List<WeeklyRoutineCurrentResponseDto.ExerciseDto> exercises = exerciseRoutines.stream()
                    .map(exerciseRoutine -> WeeklyRoutineCurrentResponseDto.ExerciseDto.from(
                            exerciseRoutine))
                    .toList();

            WeeklyRoutineCurrentResponseDto.DailyDto dailyPlan =
                    WeeklyRoutineCurrentResponseDto.DailyDto.of(dailyRoutine, exercises);

            weeklyPlan.add(dailyPlan);

        }

        return WeeklyRoutineCurrentResponseDto.of(weeklyRoutine, weeklyPlan);

    }
}
