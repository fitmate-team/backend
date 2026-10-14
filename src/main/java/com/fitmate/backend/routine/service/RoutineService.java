package com.fitmate.backend.routine.service;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.domain.RoutineExercise;
import com.fitmate.backend.routine.dto.request.RoutineWeightUpdateRequestDto;
import com.fitmate.backend.routine.repository.RoutineExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineExerciseRepository routineExerciseRepository;

    // 중량 설정
    @Transactional
    public void updateRoutineWeights(
            Long memberId,
            RoutineWeightUpdateRequestDto request
    ) {

        for (RoutineWeightUpdateRequestDto.ExerciseWeightDto exerciseDto
                : request.getExercises()) {

            RoutineExercise routineExercise = routineExerciseRepository
                    .findById(exerciseDto.getRoutineExerciseId())
                    .orElseThrow(() -> new CustomException(
                            ErrorCode.ROUTINE_EXERCISE_NOT_FOUND
                    ));

            // 로그인한 회원의 루틴인지 검증
            Long ownerId = routineExercise.getDailyRoutine()
                    .getWeeklyRoutine()
                    .getMember()
                    .getId();

            if (!ownerId.equals(memberId)) {
                throw new CustomException(
                        ErrorCode.ROUTINE_ACCESS_DENIED
                );
            }

            // 중량 수정
            routineExercise.updateWeight(exerciseDto.getWeightKg());
        }
    }
}