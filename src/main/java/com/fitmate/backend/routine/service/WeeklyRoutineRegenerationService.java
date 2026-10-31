package com.fitmate.backend.routine.service;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.WeeklyRoutine;
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
import java.util.HashSet;
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
        // 오늘 날짜 기반 월요일 찾기
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        WeeklyRoutine weeklyRoutine = weeklyRoutineRepository.findByMember_IdAndWeekStartDate(
                        memberId,
                        weekStartDate)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ROUTINE_NOT_FOUND));

        return weeklyRoutine; // 이번주 전체 계획 엔티티 객체 하나 return
    }


    // 기존 운동 기록 확인하고 절대 변경하면 안 되는 DailyRoutine 조회
    public List<DailyRoutine> getProtectedDailyRoutines(Long memberId) {
        WeeklyRoutine weeklyRoutine = getCurrentWeeklyRoutine(memberId); // 이번주 WeeklyRoutine 가져옴
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 이번주 주간 루틴에 연결된 일일 루틴 전부 조회
        List<DailyRoutine> dailyRoutines =
                dailyRoutineRepository.findByWeeklyRoutine_IdOrderByRoutineDateAsc(
                weeklyRoutine.getId());

        // DailyRoutine 아이디 추출
        List<Long> dailyRoutineIds = dailyRoutines.stream().map(DailyRoutine::getId).toList();

        // 일일 루틴 아이디로 그날 실제 운동 기록 조회
        // 없을 수 있음 (삼항 연산자 처리)
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


    // 이번 주 변경 가능한 요일 계산 (건드려도 되는 요일)
    public Set<DayOfWeek> getEditableDays(Long memberId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        // 보호해야하는 루틴 목록 가져옴
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

        return editableDays;
    }


    // 프론트가 재생성 대상으로 선택한 요일 검증
    public void validateSelectedDays(Long memberId, List<DayOfWeek> selectedDays) {
        if (selectedDays == null || selectedDays.isEmpty()) {
            throw new CustomException(ErrorCode.NO_AVAILABLE_ROUTINE_DAY);
        }

        if (selectedDays.contains(null)) {
            throw new CustomException(ErrorCode.INVALID_WEEKLY_ROUTINE_DAY);
        }

        Set<DayOfWeek> uniqueDays = new HashSet<>(selectedDays);
        if (uniqueDays.size() != selectedDays.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_WEEKLY_ROUTINE_DAY);

        }

        // 변경 가능한 요일이 맞는지 확인
        Set<DayOfWeek> editableDays = getEditableDays(memberId);
        if (!editableDays.containsAll(selectedDays)) {
            throw new CustomException(ErrorCode.ROUTINE_REGENERATION_DAY_PROTECTED);
        }
    }
}
