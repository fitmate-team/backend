package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.dto.request.RoutineWeightUpdateRequestDto;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoutineWeightService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final WorkoutRecordRepository workoutRecordRepository;

    @Transactional
    public void updateRoutineWeights(Long memberId, RoutineWeightUpdateRequestDto request) {
        if (request == null || request.getExercises() == null || request.getExercises().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_ROUTINE_WEIGHT_UPDATE);
        }

        LocalDate today = LocalDate.now(KOREA_ZONE);
        Set<Long> seenIds = new HashSet<>();

        for (RoutineWeightUpdateRequestDto.ExerciseWeightDto exerciseDto : request.getExercises()) {

            // 요청 값과 중복 운동 ID 검사
            if (exerciseDto == null || exerciseDto.getRoutineExerciseId() == null ||
                    exerciseDto.getWeightKg() == null || exerciseDto.getWeightKg() <= 0 ||
                    !seenIds.add(exerciseDto.getRoutineExerciseId())) {
                throw new CustomException(ErrorCode.INVALID_ROUTINE_WEIGHT_UPDATE);
            }

            ExerciseRoutine exerciseRoutine =
                    exerciseRoutineRepository.findById(exerciseDto.getRoutineExerciseId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_EXERCISE_NOT_FOUND));

            // 1. 로그인한 회원의 운동인지 확인
            Long ownerId = exerciseRoutine.getDailyRoutine().getWeeklyRoutine().getMember().getId();

            if (!ownerId.equals(memberId)) {
                throw new CustomException(ErrorCode.ROUTINE_ACCESS_DENIED);
            }

            // 2. 오늘 예정된 운동인지 확인
            if (!exerciseRoutine.getDailyRoutine().getRoutineDate().equals(today)) {
                throw new CustomException(ErrorCode.INVALID_ROUTINE_WEIGHT_UPDATE);
            }

            // 3. 중량을 사용하는 운동인지 확인
            if (exerciseRoutine.getExercise().getBaselineRecordType() !=
                    ExerciseBaselineType.WEIGHT_REPS_SETS) {
                throw new CustomException(ErrorCode.INVALID_ROUTINE_WEIGHT_UPDATE);
            }

            // 4. 이미 종료된 운동이면 중량 변경 금지
            boolean isFinished =
                    workoutRecordRepository.findByDailyRoutine_Id(exerciseRoutine.getDailyRoutine()
                                                                                       .getId())
                    .map(record -> record.isFinished())
                    .orElse(false);

            if (isFinished) {
                throw new CustomException(ErrorCode.WORKOUT_ALREADY_FINISHED);
            }

            // 5. 검증 통과 후 정수 중량 반영
            exerciseRoutine.updateWeight(exerciseDto.getWeightKg());
        }
    }
}
