package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentInputDto;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentInputItemDto;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentOutputDto;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentOutputItemDto;
import com.fitmate.backend.workout.domain.CalculatedDifficulty;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ExerciseAdjustmentValidator {

    // AI 입력과 응답을 비교하여 운동 목록, 응답 형식, 조정 방향을 검증한다.
    public void validate(
            ExerciseAdjustmentInputDto input,
            ExerciseAdjustmentOutputDto output
    ) {
        if (input == null || input.getExerciseRecords() == null) {
            throw new IllegalArgumentException("AI 입력 운동 기록이 없습니다.");
        }

        if (output == null || output.getExercises() == null) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 입력 기록 중 EASY·HARD 운동을 exerciseId 기준으로 정리한다.
        Map<Long, ExerciseAdjustmentInputItemDto> targetExercises =
                getTargetExercises(input.getExerciseRecords());

        Set<Long> returnedExerciseIds = new HashSet<>();

        for (ExerciseAdjustmentOutputItemDto item : output.getExercises()) {
            if (item == null || item.getExerciseId() == null) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            Long exerciseId = item.getExerciseId();
            ExerciseAdjustmentInputItemDto target = targetExercises.get(exerciseId);

            // 요청하지 않은 운동이거나 조정 대상이 아닌 운동이면 예외를 발생시킨다.
            if (target == null) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            // 동일한 운동이 응답에 두 번 이상 포함되면 예외를 발생시킨다.
            if (!returnedExerciseIds.add(exerciseId)) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            // 기록 방식에 맞는 응답인지 확인한 뒤 조정 방향을 검증한다.
            validateExerciseFormat(target, item);
            validateAdjustmentDirection(target, item);
        }

        // EASY·HARD 운동 중 응답에서 누락된 운동이 있는지 확인한다.
        if (!returnedExerciseIds.equals(targetExercises.keySet())) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }

    // EASY·HARD 운동을 추출하고 운동 ID 중복 여부를 확인한다.
    private Map<Long, ExerciseAdjustmentInputItemDto> getTargetExercises(
            List<ExerciseAdjustmentInputItemDto> exerciseRecords
    ) {
        Map<Long, ExerciseAdjustmentInputItemDto> targetExercises = new HashMap<>();

        for (ExerciseAdjustmentInputItemDto record : exerciseRecords) {
            if (record == null
                    || record.getExerciseId() == null
                    || record.getCalculatedDifficulty() == null) {
                throw new IllegalArgumentException(
                        "AI 입력 운동 기록이 올바르지 않습니다."
                );
            }

            // NORMAL·UNMEASURABLE 운동은 조정 추천 대상에서 제외한다.
            if (!isAdjustmentTarget(record.getCalculatedDifficulty())) {
                continue;
            }

            // 같은 exerciseId가 이미 있으면 이전 값이 반환된다.
            ExerciseAdjustmentInputItemDto previous =
                    targetExercises.putIfAbsent(record.getExerciseId(), record);

            if (previous != null) {
                throw new IllegalArgumentException(
                        "동일한 운동이 AI 입력에 중복되었습니다."
                );
            }
        }

        return targetExercises;
    }

    // EASY 또는 HARD일 때만 조정 추천 대상으로 판단한다.
    private boolean isAdjustmentTarget(CalculatedDifficulty difficulty) {
        return difficulty == CalculatedDifficulty.EASY
                || difficulty == CalculatedDifficulty.HARD;
    }

    // 운동 기록 방식에 따라 AI 응답의 필드와 값이 올바른지 검증한다.
    private void validateExerciseFormat(
            ExerciseAdjustmentInputItemDto input,
            ExerciseAdjustmentOutputItemDto output
    ) {
        // 모든 운동은 기록 방식, 양의 세트 수, 추천 이유가 필요하다.
        if (input.getBaselineRecordType() == null
                || output.getSetCount() == null
                || output.getSetCount() <= 0
                || output.getReason() == null
                || output.getReason().isBlank()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        ExerciseBaselineType type = input.getBaselineRecordType();

        // 기록 방식별로 사용 가능한 필드와 값의 범위를 확인한다.
        if (type == ExerciseBaselineType.WEIGHT_REPS_SETS) {
            validateWeightRepsSets(input, output);
        } else if (type == ExerciseBaselineType.REPS_SETS) {
            validateRepsSets(output);
        } else if (type == ExerciseBaselineType.DURATION) {
            validateDuration(output);
        } else {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }

    // 중량·횟수·세트 수 기반 운동의 응답 형식을 검증한다.
    private void validateWeightRepsSets(
            ExerciseAdjustmentInputItemDto input,
            ExerciseAdjustmentOutputItemDto output
    ) {
        // 반복 횟수는 필수이고, 시간은 사용하지 않는다.
        if (output.getReps() == null
                || output.getReps() <= 0
                || output.getDurationSeconds() != null) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        BigDecimal inputWeight = input.getWeightKg();
        BigDecimal outputWeight = output.getWeightKg();

        // 기존 중량이 없으면 AI가 임의로 중량을 만들지 않아야 한다.
        if (inputWeight == null) {
            if (outputWeight != null) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        } else if (outputWeight == null
                || outputWeight.compareTo(BigDecimal.ZERO) <= 0) {
            // 기존 중량이 있으면 추천 중량도 0보다 커야 한다.
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }

    // 횟수·세트 수 기반 운동의 응답 형식을 검증한다.
    private void validateRepsSets(ExerciseAdjustmentOutputItemDto output) {
        // 반복 횟수는 필수이고, 중량과 시간은 사용하지 않는다.
        if (output.getWeightKg() != null
                || output.getReps() == null
                || output.getReps() <= 0
                || output.getDurationSeconds() != null) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }

    // 시간 기반 운동의 응답 형식을 검증한다.
    private void validateDuration(ExerciseAdjustmentOutputItemDto output) {
        // 중량과 횟수는 사용하지 않고, 세트 수는 반드시 1이어야 한다.
        if (output.getWeightKg() != null
                || output.getReps() != null
                || output.getSetCount() == null
                || output.getSetCount() != 1
                || output.getDurationSeconds() == null
                || output.getDurationSeconds() <= 0) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }

    // AI가 EASY·HARD에 맞는 방향으로 운동 값을 조정했는지 검증한다.
    private void validateAdjustmentDirection(
            ExerciseAdjustmentInputItemDto input,
            ExerciseAdjustmentOutputItemDto output
    ) {
        boolean isEasy = input.getCalculatedDifficulty() == CalculatedDifficulty.EASY;

        // 시간 기반 운동은 운동 시간만 비교한다.
        if (input.getBaselineRecordType() == ExerciseBaselineType.DURATION) {
            validateDurationDirection(input, output, isEasy);
            return;
        }

        boolean weightChangedInDirection = false;

        // 기존 중량과 추천 중량이 모두 있을 때만 중량을 비교한다.
        if (input.getWeightKg() != null && output.getWeightKg() != null) {
            int comparison = output.getWeightKg().compareTo(input.getWeightKg());

            // EASY는 증가, HARD는 감소해야 한다.
            weightChangedInDirection = isEasy
                    ? comparison > 0
                    : comparison < 0;
        }

        // 기존 횟수와 세트 수가 없으면 조정 방향을 판단할 수 없다.
        if (input.getReps() == null || input.getSetCount() == null) {
            throw new IllegalArgumentException(
                    "AI 입력 운동 기록이 올바르지 않습니다."
            );
        }

        // EASY는 횟수가 늘었는지, HARD는 횟수가 줄었는지 확인한다.
        boolean repsChangedInDirection = isEasy
                ? output.getReps() > input.getReps()
                : output.getReps() < input.getReps();

        // EASY는 세트 수가 늘었는지, HARD는 세트 수가 줄었는지 확인한다.
        boolean setCountChangedInDirection = isEasy
                ? output.getSetCount() > input.getSetCount()
                : output.getSetCount() < input.getSetCount();

        // 중량·횟수·세트 수 중 하나라도 원하는 방향으로 바뀌어야 한다.
        if (!weightChangedInDirection
                && !repsChangedInDirection
                && !setCountChangedInDirection) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }

    // 시간 기반 운동의 추천 시간이 난이도에 맞게 바뀌었는지 검증한다.
    private void validateDurationDirection(
            ExerciseAdjustmentInputItemDto input,
            ExerciseAdjustmentOutputItemDto output,
            boolean isEasy
    ) {
        if (input.getDurationSeconds() == null) {
            throw new IllegalArgumentException("AI 입력 운동 시간이 없습니다.");
        }

        // EASY는 시간 증가, HARD는 시간 감소가 필요하다.
        boolean changedInDirection = isEasy
                ? output.getDurationSeconds() > input.getDurationSeconds()
                : output.getDurationSeconds() < input.getDurationSeconds();

        if (!changedInDirection) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }
}