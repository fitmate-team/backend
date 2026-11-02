package com.fitmate.backend.home.service;

import com.fitmate.backend.home.dto.response.HomeResponseDto;
import com.fitmate.backend.home.dto.response.HomeResponseDto.TodayWorkoutResponseDto;
import com.fitmate.backend.home.dto.response.HomeResponseDto.TodayWorkoutStatus;
import com.fitmate.backend.home.dto.response.HomeResponseDto.WeeklyWorkoutResponseDto;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.domain.WeeklyRoutine;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.routine.repository.WeeklyRoutineRepository;
import com.fitmate.backend.workout.domain.WorkoutRecord;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    // 서버의 기본 시간대와 관계없이 한국 날짜를 기준으로 조회합니다.
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final WeeklyRoutineRepository weeklyRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final WorkoutRecordRepository workoutRecordRepository;

    /**
     * 홈 화면에 필요한 이번 주 운동 현황과 오늘의 운동 정보를 조회합니다.
     */
    public HomeResponseDto getHome(Long memberId) {

        // 1. 한국 시간 기준 오늘 날짜를 구합니다.
        LocalDate today = LocalDate.now(KOREA_ZONE);

        // 2. 이번 주의 시작 날짜를 구합니다.
        // 오늘이 월요일이면 오늘, 아니라면 가장 가까운 이전 월요일입니다.
        LocalDate weekStart = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        // 3. 로그인한 회원의 이번 주 루틴을 조회합니다.
        // 루틴이 없어도 홈 화면은 정상 응답해야 하므로 예외를 발생시키지 않습니다.
        Optional<WeeklyRoutine> weeklyRoutine =
                weeklyRoutineRepository.findByMember_IdAndWeekStartDate(
                        memberId,
                        weekStart
                );

        // 4. 주간 경계와 관계없이 추천된 운동일 기준 연속 완료 횟수를 계산합니다.
        int consecutiveWorkoutDays =
                calculateConsecutiveWorkoutDays(memberId, today);

        // 5. 이번 주 완료 횟수, 목표 횟수, 연속 완료 횟수를 조합합니다.
        WeeklyWorkoutResponseDto weeklyWorkout =
                getWeeklyWorkout(
                        memberId,
                        weekStart,
                        weeklyRoutine,
                        consecutiveWorkoutDays
                );

        // 6. 오늘 예정된 운동의 요약과 완료 여부를 조회합니다.
        TodayWorkoutResponseDto todayWorkout =
                getTodayWorkout(memberId, today);

        // 7. 두 영역의 데이터를 하나의 홈 응답으로 반환합니다.
        return HomeResponseDto.of(
                weeklyWorkout,
                todayWorkout
        );
    }

    /**
     * 이번 주 운동 현황을 반환합니다.
     * 목표 횟수는 이번 주 루틴에 포함된 일일 루틴 수입니다.
     */
    private WeeklyWorkoutResponseDto getWeeklyWorkout(
            Long memberId,
            LocalDate weekStart,
            Optional<WeeklyRoutine> weeklyRoutine,
            int consecutiveWorkoutDays
    ) {

        // 이번 주 루틴이 있으면 해당 루틴의 일일 운동 수를 목표 횟수로 사용합니다.
        // 이번 주 루틴이 없으면 목표 횟수는 0입니다.
        int targetWorkoutCount = weeklyRoutine
                .map(routine ->
                        dailyRoutineRepository
                                .findByWeeklyRoutine_IdOrderByRoutineDateAsc(
                                        routine.getId()
                                )
                                .size()
                )
                .orElse(0);

        // 운동 시작 일시가 이번 주 범위에 속하고 완료 처리된 기록을 집계합니다.
        // 이번 주 월요일 00:00은 포함하고 다음 주 월요일 00:00은 제외합니다.
        int completedWorkoutCount = Math.toIntExact(
                workoutRecordRepository
                        .countFinishedWorkoutsByMemberIdAndStartedAtRange(
                                memberId,
                                weekStart.atStartOfDay(),
                                weekStart.plusWeeks(1).atStartOfDay()
                        )
        );

        // 연속 완료 횟수는 이번 주 루틴 존재 여부와 관계없이 유지합니다.
        return WeeklyWorkoutResponseDto.of(
                weeklyRoutine.isPresent(),
                completedWorkoutCount,
                targetWorkoutCount,
                consecutiveWorkoutDays
        );
    }

    /**
     * 오늘의 운동 요약과 완료 여부를 반환합니다.
     * 오늘 예정된 운동이 없으면 빈 운동 정보를 반환합니다.
     */
    private TodayWorkoutResponseDto getTodayWorkout(
            Long memberId,
            LocalDate today
    ) {

        // 1. 회원 ID와 오늘 날짜로 예정된 일일 루틴을 조회합니다.
        Optional<DailyRoutine> dailyRoutineOptional =
                dailyRoutineRepository
                        .findByWeeklyRoutine_Member_IdAndRoutineDate(
                                memberId,
                                today
                        );

        // 오늘 운동이 없는 경우에도 예외 없이 정상 응답합니다.
        // 이번 주 루틴 자체가 없는지는 weeklyWorkout.hasWeeklyRoutine으로 구분합니다.
        if (dailyRoutineOptional.isEmpty()) {
            return TodayWorkoutResponseDto.empty(today);
        }

        DailyRoutine dailyRoutine = dailyRoutineOptional.get();

        // 2. 오늘 루틴에 포함된 운동을 수행 순서대로 조회합니다.
        List<ExerciseRoutine> exerciseRoutines =
                exerciseRoutineRepository
                        .findByDailyRoutine_IdOrderByExerciseOrderAsc(
                                dailyRoutine.getId()
                        );

        // 3. 홈 카드에서 표시할 운동명 목록을 만듭니다.
        // 조회 순서를 유지하므로 프론트에서 앞의 운동명부터 표시할 수 있습니다.
        List<String> exerciseNames = exerciseRoutines.stream()
                .map(exerciseRoutine ->
                        exerciseRoutine.getExercise().getNameKo()
                )
                .toList();

        // 4. 오늘 루틴에 연결된 운동 기록을 조회합니다.
        // 운동을 시작하지 않았다면 기록이 존재하지 않습니다.
        Optional<WorkoutRecord> workoutRecord =
                workoutRecordRepository.findByDailyRoutine_Id(
                        dailyRoutine.getId()
                );

        // 5. 완료된 기록만 COMPLETED로 반환합니다.
        // 기록이 없거나 시작만 하고 종료하지 않았다면 NOT_STARTED로 반환합니다.
        TodayWorkoutStatus status = workoutRecord
                .filter(WorkoutRecord::isFinished)
                .map(record -> TodayWorkoutStatus.COMPLETED)
                .orElse(TodayWorkoutStatus.NOT_STARTED);

        // 운동 기록이 있으면 ID를 전달하고, 없으면 null을 전달합니다.
        Long workoutRecordId = workoutRecord
                .map(WorkoutRecord::getId)
                .orElse(null);

        // 6. 루틴 요약, 운동명 목록, 평균 휴식 시간을 응답 DTO로 변환합니다.
        return TodayWorkoutResponseDto.of(
                status,
                today,
                dailyRoutine.getId(),
                workoutRecordId,
                dailyRoutine.getMainTarget().getDisplayTitle(),
                exerciseNames,
                dailyRoutine.getEstimatedDurationMinutes(),
                calculateAverageRestSeconds(exerciseRoutines)
        );
    }

    /**
     * 추천된 운동일을 기준으로 연속 완료 횟수를 계산합니다.
     *
     * 오늘 미완료 운동은 건너뛰어 기존 연속 횟수를 유지합니다.
     * 과거 미완료 운동을 만나면 계산을 종료합니다.
     * 운동이 없는 날과 주 변경은 연속 횟수에 영향을 주지 않습니다.
     */
    private int calculateConsecutiveWorkoutDays(
            Long memberId,
            LocalDate today
    ) {

        // 1. 오늘까지 예정된 운동을 날짜 내림차순으로 조회합니다.
        // 미래 운동은 제외하고, 지난주의 운동도 함께 조회합니다.
        List<DailyRoutine> dailyRoutines =
                dailyRoutineRepository
                        .findRoutinesForConsecutiveWorkoutCount(
                                memberId,
                                today
                        );

        // 예정된 운동 이력이 없으면 연속 완료 횟수는 0입니다.
        // 빈 ID 목록으로 운동 기록 조회를 호출하지 않도록 바로 반환합니다.
        if (dailyRoutines.isEmpty()) {
            return 0;
        }

        // 2. 조회한 일일 루틴의 ID 목록을 만듭니다.
        List<Long> dailyRoutineIds = dailyRoutines.stream()
                .map(DailyRoutine::getId)
                .toList();

        // 3. 해당 루틴들의 운동 기록을 한 번에 조회합니다.
        // 루틴마다 별도로 운동 기록을 조회하는 반복 쿼리를 방지합니다.
        // 일일 루틴 ID를 키로 사용해 반복문에서 기록을 바로 찾습니다.
        Map<Long, WorkoutRecord> workoutRecordMap =
                workoutRecordRepository
                        .findByDailyRoutine_IdIn(dailyRoutineIds)
                        .stream()
                        .collect(Collectors.toMap(
                                record -> record.getDailyRoutine().getId(),
                                Function.identity()
                        ));

        int consecutiveWorkoutDays = 0;

        // 4. 가장 최근 추천 운동일부터 과거 방향으로 완료 여부를 확인합니다.
        for (DailyRoutine dailyRoutine : dailyRoutines) {
            WorkoutRecord workoutRecord =
                    workoutRecordMap.get(dailyRoutine.getId());

            // 기록이 존재하면서 종료 처리까지 되어야 완료로 인정합니다.
            boolean completed = workoutRecord != null
                    && workoutRecord.isFinished();

            // 완료한 추천 운동일마다 연속 횟수를 1씩 증가시킵니다.
            if (completed) {
                consecutiveWorkoutDays++;
                continue;
            }

            // 오늘은 아직 운동할 시간이 남아 있으므로 미완료여도 건너뜁니다.
            // 이후 과거 운동들을 확인해 기존 연속 횟수를 유지합니다.
            if (dailyRoutine.getRoutineDate().equals(today)) {
                continue;
            }

            // 과거 추천 운동일이 미완료라면 해당 지점에서 연속 기록이 끊깁니다.
            // 그보다 이전의 완료 기록은 현재 연속 횟수에 포함하지 않습니다.
            break;
        }

        return consecutiveWorkoutDays;
    }

    /**
     * 휴식 시간이 설정된 운동들의 평균 휴식 시간을 초 단위로 계산합니다.
     * 소수점은 반올림하며, 계산할 값이 없으면 0을 반환합니다.
     */
    private int calculateAverageRestSeconds(
            List<ExerciseRoutine> exerciseRoutines
    ) {

        // 휴식 시간이 null인 운동은 평균 계산 대상에서 제외합니다.
        // 설정된 값이 0초인 운동은 계산 대상에 포함합니다.
        double averageRestSeconds = exerciseRoutines.stream()
                .map(ExerciseRoutine::getRestSeconds)
                .filter(restSeconds -> restSeconds != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        // 화면에서 정수 초 단위로 표시하도록 반올림합니다.
        return (int) Math.round(averageRestSeconds);
    }
}