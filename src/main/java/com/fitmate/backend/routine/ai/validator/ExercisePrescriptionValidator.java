package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.input.*;
import com.fitmate.backend.routine.ai.dto.output.ExerciseRoutineOutputDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ExercisePrescriptionValidator {

    // 공통 처방 형식 검사
    public void validateFormat(ExerciseRoutineOutputDto exercise, AiEligibleExerciseDto candidate) {

        if (exercise == null || candidate == null || exercise.getSets() == null ||
                exercise.getSets() <= 0 || exercise.getRestSeconds() == null ||
                exercise.getRestSeconds() < 0) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        ExerciseBaselineType type = candidate.getBaselineRecordType();

        if (type == ExerciseBaselineType.WEIGHT_REPS_SETS) {
            if (exercise.getReps() == null || exercise.getReps() <= 0 ||
                    exercise.getDurationSeconds() != null || (exercise.getWeightKg() != null &&
                    exercise.getWeightKg().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        }

        if (type == ExerciseBaselineType.REPS_SETS) {
            if (exercise.getReps() == null || exercise.getReps() <= 0 ||
                    exercise.getDurationSeconds() != null || exercise.getWeightKg() != null) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        }

        if (type == ExerciseBaselineType.DURATION) {
            if (exercise.getReps() != null || exercise.getDurationSeconds() == null ||
                    exercise.getDurationSeconds() <= 0 || exercise.getWeightKg() != null) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        }

        // 유산소 운동 검사
        if (candidate.getExerciseType() == ExerciseType.CARDIO) {
            if (exercise.getSets() != 1 || exercise.getReps() != null ||
                    exercise.getDurationSeconds() == null || exercise.getDurationSeconds() <= 0 ||
                    exercise.getWeightKg() != null || exercise.getRestSeconds() != 0) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        }
    }

    // 주간 생성 중량 검사
    public void validateWeeklyWeight(ExerciseRoutineOutputDto exercise,
                                     AiEligibleExerciseDto candidate,
                                     List<AiRecentExerciseRecordDto> records) {

        if (candidate.getBaselineRecordType() != ExerciseBaselineType.WEIGHT_REPS_SETS) {
            return;
        }

        Integer recentWeightKg = records.stream()
                .filter(record -> exercise.getExerciseCode().equals(record.getExerciseCode()) &&
                        record.getWeightKg() != null)
                .map(AiRecentExerciseRecordDto::getWeightKg)
                .findFirst()
                .orElse(null);

        BigDecimal weightKg = exercise.getWeightKg();

        if (recentWeightKg == null) {
            if (weightKg != null) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        } else {
            if (weightKg == null || weightKg.compareTo(BigDecimal.valueOf(recentWeightKg)) != 0) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        }
    }

    // 당일 재추천 중량 검사
    public void validateDailyWeight(ExerciseRoutineOutputDto exercise,
                                    AiEligibleExerciseDto candidate,
                                    List<AiRecentExerciseRecordDto> records,
                                    AiDailyOriginalExerciseDto original) {

        if (candidate.getBaselineRecordType() != ExerciseBaselineType.WEIGHT_REPS_SETS) {
            return;
        }

        // 기존 운동을 유지한 경우
        if (exercise.getExerciseCode().equals(original.getExerciseCode())) {
            BigDecimal originalWeight = original.getWeightKg();
            BigDecimal newWeight = exercise.getWeightKg();

            if ((originalWeight == null && newWeight != null) || (originalWeight != null &&
                    (newWeight == null || originalWeight.compareTo(newWeight) != 0))) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        } else {
            // 다른 운동으로 치환한 경우
            validateWeeklyWeight(exercise, candidate, records);
        }
    }
}
