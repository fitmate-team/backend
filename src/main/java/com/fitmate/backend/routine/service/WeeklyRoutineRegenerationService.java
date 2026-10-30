package com.fitmate.backend.routine.service;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.domain.WeeklyRoutine;
import com.fitmate.backend.routine.dto.response.WeeklyRoutineResponseDto;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyRoutineRegenerationService {

    private final WeeklyRoutineRepository weeklyRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;
    private final WorkoutRecordRepository workoutRecordRepository;

    // 이번 주에 이미 저장된 WeeklyRoutine 조회
    public WeeklyRoutine getCurrentWeeklyRoutine(Long memberId) {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        WeeklyRoutine weeklyRoutine = weeklyRoutineRepository.findByMember_IdAndWeekStartDate(
                        memberId,
                        weekStartDate)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ROUTINE_NOT_FOUND));
        return weeklyRoutine;
    }


    // 기존 주간에서 절대 변경하면 안 되는 DailyRoutine 조회
    public List<DailyRoutine> getProtectedDailyRoutines(Long memberId) {

        WeeklyRoutine weeklyRoutine = getCurrentWeeklyRoutine(memberId);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<DailyRoutine> dailyRoutines =
                dailyRoutineRepository.findByWeeklyRoutine_IdOrderByRoutineDateAsc(
                weeklyRoutine.getId());
        List<Long> dailyRoutineIds = dailyRoutines.stream().map(DailyRoutine::getId).toList();
        List<WorkoutRecord> workoutRecords = workoutRecordRepository.findByDailyRoutine_IdIn(
                dailyRoutineIds);

        if (workoutRecords.stream().anyMatch(workoutRecord -> !workoutRecord.isFinished())) {
            throw new CustomException(ErrorCode.WORKOUT_ALREADY_STARTED);
        }

        Set<Long> recordedDailyRoutineIds = workoutRecords.stream()
                .map(workoutRecord -> workoutRecord.getDailyRoutine().getId())
                .collect(Collectors.toSet());

        List<DailyRoutine> protectedDailyRoutines = dailyRoutines.stream()
                .filter(d -> d.getRoutineDate().isBefore(today))
                .filter(e -> recordedDailyRoutineIds.contains(e.getId()))
                .toList();

        return protectedDailyRoutines;
    }


    // 3. 이번 주에서 새로 생성하거나 수정할 수 있는 요일 계산
    public Set<DayOfWeek> getEditableDays(Long memberId) {

        // 3-1. Asia/Seoul 기준 오늘 날짜를 구한다.
        //      변수명: today

        // 3-2. 보호해야 하는 기존 DailyRoutine을 가져온다.
        //      변수명: protectedDailyRoutines
        //      호출 메서드: getProtectedDailyRoutines(memberId)
        //
        //      이 과정에서 운동 진행 중인지도 검사된다.

        // 3-3. 보호 대상 DailyRoutine에서 요일만 추출한다.
        //      변수명: protectedDays
        //      타입: Set<DayOfWeek>
        //
        //      힌트: dailyRoutine.getRoutineDate().getDayOfWeek()
        //      힌트: stream().map().collect(Collectors.toSet())

        // 3-4. 변경 가능한 요일을 담을 Set을 만든다.
        //      변수명: editableDays
        //      타입: Set<DayOfWeek>
        //      힌트: HashSet 또는 EnumSet

        // 3-5. 월요일부터 일요일까지 모든 요일을 순회한다.
        //      힌트: DayOfWeek.values()

        // 3-6. 각 요일이 다음 조건을 모두 만족하면 editableDays에 추가한다.
        //
        //      조건 A. 오늘이거나 오늘 이후의 요일이다.
        //      힌트: day.getValue() >= today.getDayOfWeek().getValue()
        //
        //      조건 B. protectedDays에 포함되지 않는다.
        //      힌트: !protectedDays.contains(day)
        //
        //      중요: 기존 DailyRoutine이 없는 미래 요일도
        //      새로 운동을 만들 수 있으므로 포함해야 한다.

        // 3-7. editableDays가 비었다면 예외를 발생시킨다.
        //      예외: CustomException(ErrorCode.NO_AVAILABLE_ROUTINE_DAY)

        // 3-8. editableDays를 반환한다.

        throw new UnsupportedOperationException("TODO");
    }


    // 4. 사용자가 재생성 대상으로 선택한 요일 검증
    public void validateSelectedDays(Long memberId, List<DayOfWeek> selectedDays) {

        // 4-1. selectedDays가 null 또는 빈 리스트인지 확인한다.
        //      비어 있으면 NO_AVAILABLE_ROUTINE_DAY 예외 발생

        // 4-2. selectedDays에 null 요일이 포함되었는지 확인한다.
        //      포함되었다면 INVALID_WEEKLY_ROUTINE_DAY 예외 발생

        // 4-3. selectedDays에 중복 요일이 있는지 확인한다.
        //      힌트: HashSet은 중복 값을 허용하지 않는다.
        //      힌트: List의 크기와 Set의 크기를 비교해 본다.
        //
        //      중복이 있으면 DUPLICATE_WEEKLY_ROUTINE_DAY 예외 발생

        // 4-4. getEditableDays(memberId)를 호출한다.
        //      변수명: editableDays

        // 4-5. selectedDays의 모든 요일이 editableDays에 있는지 확인한다.
        //      힌트: containsAll()
        //
        //      선택한 요일 중 하나라도 수정 불가능하다면
        //      ROUTINE_REGENERATION_DAY_PROTECTED 예외 발생

        // 4-6. 모든 검사를 통과했다면 그대로 메서드를 종료한다.
        //      void 메서드이므로 반환값은 필요 없다.
    }
}
