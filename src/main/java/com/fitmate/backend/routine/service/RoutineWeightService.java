package com.fitmate.backend.routine.service;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.dto.request.RoutineWeightUpdateRequestDto;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineWeightService {

    private final ExerciseRoutineRepository exerciseRoutineRepository;

    // 중량 설정
    @Transactional
    public void updateRoutineWeights(
            Long memberId,
            RoutineWeightUpdateRequestDto request
    ) {

        for (RoutineWeightUpdateRequestDto.ExerciseWeightDto exerciseDto
                : request.getExercises()) {

            ExerciseRoutine exerciseRoutine = exerciseRoutineRepository
                    .findById(exerciseDto.getRoutineExerciseId())
                    .orElseThrow(() -> new CustomException(
                            ErrorCode.ROUTINE_EXERCISE_NOT_FOUND
                    ));

            // 로그인한 회원의 루틴인지 검증
            Long ownerId = exerciseRoutine.getDailyRoutine()
                    .getWeeklyRoutine()
                    .getMember()
                    .getId();

            if (!ownerId.equals(memberId)) {
                throw new CustomException(
                        ErrorCode.ROUTINE_ACCESS_DENIED
                );
            }

            // 중량 수정
            exerciseRoutine.updateWeight(exerciseDto.getWeightKg());
        }
    }
}