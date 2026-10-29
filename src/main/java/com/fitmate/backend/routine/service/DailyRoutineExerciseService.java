package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.dto.response.DailyRoutineAvailableExerciseResponseDto;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyRoutineExerciseService {

    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final ExerciseRepository exerciseRepository;

    /**
     * 해당 회원의 데일리 루틴에 설정된 운동 환경에서 가능한 운동을 조회합니다.
     * 각 운동에 해당 데일리 루틴의 포함 여부를 함께 반환합니다.
     */
    public DailyRoutineAvailableExerciseResponseDto getAvailableExercises(
            Long memberId,
            Long dailyRoutineId
    ) {
        // 데일리 루틴과 운동 환경을 조회하면서 회원의 소유권을 확인합니다.
        DailyRoutine dailyRoutine = dailyRoutineRepository
                .findByIdAndWeeklyRoutine_Member_Id(dailyRoutineId, memberId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.DAILY_ROUTINE_NOT_FOUND)
                );

        WorkoutEnvironment environment = dailyRoutine.getWorkoutEnvironment();

        // 해당 장소에서 가능한 운동 중 보유 기구로 수행할 수 있는 운동을 조회합니다.
        List<Exercise> availableExercises = exerciseRepository
                .findAvailableByLocationType(environment.getLocationType())
                .stream()
                .filter(exercise ->
                        environment.getEquipment().containsAll(exercise.getEquipment())
                )
                .toList();

        // 해당 데일리 루틴에 포함된 운동 ID를 조회합니다.
        Set<Long> includedExerciseIds = exerciseRoutineRepository
                .findExerciseIdsByDailyRoutineId(dailyRoutineId);

        return DailyRoutineAvailableExerciseResponseDto.of(
                dailyRoutineId,
                availableExercises,
                includedExerciseIds
        );
    }
}