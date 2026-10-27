package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutineAdjustment;
import com.fitmate.backend.routine.dto.request.ExerciseAdjustmentApplyRequestDto;
import com.fitmate.backend.routine.repository.ExerciseRoutineAdjustmentRepository;
import com.fitmate.backend.workout.domain.CalculatedDifficulty;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import com.fitmate.backend.workout.domain.WorkoutRecord;
import com.fitmate.backend.workout.repository.WorkoutExerciseRecordRepository;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseRoutineAdjustmentService {

    private final ExerciseRoutineAdjustmentRepository adjustmentRepository;
    private final WorkoutRecordRepository workoutRecordRepository;
    private final WorkoutExerciseRecordRepository workoutExerciseRecordRepository;

    /**
     * 사용자가 선택한 운동 조정값을 저장하거나 기존 조정값을 갱신한다.
     */
    @Transactional
    public void applyAdjustment(
            Long memberId,
            Long workoutRecordId,
            Long exerciseId,
            ExerciseAdjustmentApplyRequestDto request
    ) {
        // 회원의 종료된 운동 기록인지 확인한다.
        WorkoutRecord workoutRecord = getFinishedWorkoutRecord(
                memberId,
                workoutRecordId
        );

        // 해당 운동이 이 운동 기록에 포함되어 있는지 확인한다.
        WorkoutExerciseRecord exerciseRecord = getExerciseRecord(
                workoutRecordId,
                exerciseId
        );

        // EASY·HARD 운동만 사용자가 조정값을 적용할 수 있다.
        validateAdjustmentTarget(exerciseRecord);

        // 운동 기록 방식에 맞는 값인지 확인한다.
        validateRequestFormat(exerciseRecord, request);

        // 기존 운동값을 기준으로 조정 방향을 확인한다.
        validateAdjustmentDirection(exerciseRecord, request);

        ExerciseRoutine exerciseRoutine = exerciseRecord.getExerciseRoutine();
        Exercise exercise = exerciseRoutine.getExercise();

        Member member = workoutRecord.getDailyRoutine()
                .getWeeklyRoutine()
                .getMember();

        // 기존 조정값이 있으면 갱신하고, 없으면 새로 저장한다.
        adjustmentRepository.findByMember_IdAndExercise_Id(memberId, exerciseId)
                .ifPresentOrElse(
                        adjustment -> adjustment.update(
                                request.getWeightKg(),
                                request.getReps(),
                                request.getSetCount(),
                                request.getDurationSeconds()
                        ),
                        () -> adjustmentRepository.save(
                                ExerciseRoutineAdjustment.builder()
                                        .member(member)
                                        .exercise(exercise)
                                        .weight(request.getWeightKg())
                                        .reps(request.getReps())
                                        .setCount(request.getSetCount())
                                        .durationSeconds(request.getDurationSeconds())
                                        .build()
                        )
                );
    }

    /**
     * 사용자가 AI 추천 사용을 선택한 운동의 기존 조정값을 삭제한다.
     */
    @Transactional
    public void deleteAdjustment(
            Long memberId,
            Long workoutRecordId,
            Long exerciseId
    ) {
        // 회원의 종료된 운동 기록인지 확인한다.
        getFinishedWorkoutRecord(memberId, workoutRecordId);

        // 해당 운동이 이 운동 기록에 포함되어 있는지 확인한다.
        WorkoutExerciseRecord exerciseRecord = getExerciseRecord(
                workoutRecordId,
                exerciseId
        );

        // 사용자 선택은 EASY·HARD 운동에 대해서만 허용한다.
        validateAdjustmentTarget(exerciseRecord);

        // 기존 조정값이 없으면 삭제할 데이터 없이 종료된다.
        adjustmentRepository.deleteByMember_IdAndExercise_Id(
                memberId,
                exerciseId
        );
    }

    /**
     * 회원의 종료된 운동 기록을 조회한다.
     */
    private WorkoutRecord getFinishedWorkoutRecord(
            Long memberId,
            Long workoutRecordId
    ) {
        return workoutRecordRepository.findFinishedRecordByIdAndMemberId(
                workoutRecordId,
                memberId
        ).orElseThrow(
                () -> new CustomException(ErrorCode.WORKOUT_RECORD_NOT_FOUND)
        );
    }

    /**
     * 운동 기록에 포함된 특정 운동의 수행 기록을 조회한다.
     */
    private WorkoutExerciseRecord getExerciseRecord(
            Long workoutRecordId,
            Long exerciseId
    ) {
        return workoutExerciseRecordRepository
                .findByWorkoutRecord_IdOrderByExerciseRoutine_ExerciseOrderAsc(
                        workoutRecordId
                )
                .stream()
                .filter(record ->
                        record.getExerciseRoutine()
                                .getExercise()
                                .getId()
                                .equals(exerciseId)
                )
                .findFirst()
                .orElseThrow(
                        () -> new CustomException(
                                ErrorCode.INVALID_EXERCISE_ADJUSTMENT
                        )
                );
    }

    /**
     * EASY·HARD 운동인지 확인한다.
     */
    private void validateAdjustmentTarget(
            WorkoutExerciseRecord exerciseRecord
    ) {
        CalculatedDifficulty difficulty =
                exerciseRecord.getCalculatedDifficulty();

        if (difficulty != CalculatedDifficulty.EASY
                && difficulty != CalculatedDifficulty.HARD) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
        }
    }

    /**
     * 운동 기록 방식에 맞게 조정값이 전달됐는지 확인한다.
     */
    private void validateRequestFormat(
            WorkoutExerciseRecord exerciseRecord,
            ExerciseAdjustmentApplyRequestDto request
    ) {
        if (request == null
                || request.getSetCount() == null
                || request.getSetCount() <= 0) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
        }

        ExerciseRoutine exerciseRoutine = exerciseRecord.getExerciseRoutine();

        ExerciseBaselineType type = exerciseRoutine.getExercise()
                .getBaselineRecordType();

        if (type == ExerciseBaselineType.WEIGHT_REPS_SETS) {
            validateWeightRepsSets(exerciseRoutine, request);
        } else if (type == ExerciseBaselineType.REPS_SETS) {
            validateRepsSets(request);
        } else if (type == ExerciseBaselineType.DURATION) {
            validateDuration(request);
        } else {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
        }
    }

    /**
     * 중량·횟수·세트 수 기반 운동의 조정값을 확인한다.
     */
    private void validateWeightRepsSets(
            ExerciseRoutine exerciseRoutine,
            ExerciseAdjustmentApplyRequestDto request
    ) {
        if (request.getReps() == null
                || request.getReps() <= 0
                || request.getDurationSeconds() != null) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
        }

        BigDecimal currentWeight = exerciseRoutine.getWeight();
        BigDecimal requestedWeight = request.getWeightKg();

        // 기존 중량이 없는 경우 추천 중량도 null이어야 한다.
        if (currentWeight == null) {
            if (requestedWeight != null) {
                throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
            }
            return;
        }

        // 기존 중량이 있는 경우 추천 중량은 0보다 커야 한다.
        if (requestedWeight == null
                || requestedWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
        }
    }

    /**
     * 횟수·세트 수 기반 운동의 조정값을 확인한다.
     */
    private void validateRepsSets(
            ExerciseAdjustmentApplyRequestDto request
    ) {
        if (request.getWeightKg() != null
                || request.getReps() == null
                || request.getReps() <= 0
                || request.getDurationSeconds() != null) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
        }
    }

    /**
     * 시간 기반 운동의 조정값을 확인한다.
     */
    private void validateDuration(
            ExerciseAdjustmentApplyRequestDto request
    ) {
        if (request.getWeightKg() != null
                || request.getReps() != null
                || request.getSetCount() == null
                || request.getSetCount() != 1
                || request.getDurationSeconds() == null
                || request.getDurationSeconds() <= 0) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
        }
    }

    /**
     * EASY·HARD에 맞게 운동값이 조정됐는지 확인한다.
     */
    private void validateAdjustmentDirection(
            WorkoutExerciseRecord exerciseRecord,
            ExerciseAdjustmentApplyRequestDto request
    ) {
        ExerciseRoutine exerciseRoutine = exerciseRecord.getExerciseRoutine();

        boolean isEasy =
                exerciseRecord.getCalculatedDifficulty() == CalculatedDifficulty.EASY;

        ExerciseBaselineType type = exerciseRoutine.getExercise()
                .getBaselineRecordType();

        if (type == ExerciseBaselineType.DURATION) {
            Integer currentDuration = exerciseRoutine.getDurationSeconds();

            if (currentDuration == null) {
                throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
            }

            boolean changedInDirection = isEasy
                    ? request.getDurationSeconds() > currentDuration
                    : request.getDurationSeconds() < currentDuration;

            if (!changedInDirection) {
                throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
            }

            return;
        }

        Integer currentReps = exerciseRoutine.getReps();
        Integer currentSetCount = exerciseRoutine.getSetCount();

        if (currentReps == null || currentSetCount == null) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
        }

        boolean weightChangedInDirection = false;

        if (exerciseRoutine.getWeight() != null
                && request.getWeightKg() != null) {
            int comparison = request.getWeightKg()
                    .compareTo(exerciseRoutine.getWeight());

            weightChangedInDirection = isEasy
                    ? comparison > 0
                    : comparison < 0;
        }

        boolean repsChangedInDirection = isEasy
                ? request.getReps() > currentReps
                : request.getReps() < currentReps;

        boolean setCountChangedInDirection = isEasy
                ? request.getSetCount() > currentSetCount
                : request.getSetCount() < currentSetCount;

        // 중량·횟수·세트 수 중 하나라도 원하는 방향으로 바뀌어야 한다.
        if (!weightChangedInDirection
                && !repsChangedInDirection
                && !setCountChangedInDirection) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_ADJUSTMENT);
        }
    }
}