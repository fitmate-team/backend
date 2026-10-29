package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.common.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.common.AiRecentExerciseRecordDto;
import com.fitmate.backend.routine.ai.dto.common.ExerciseRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.daily.AiDailyOriginalExerciseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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
                    exercise.getDurationSeconds() != null ||
                    (exercise.getWeightKg() != null && exercise.getWeightKg() <= 0)) {
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

        if (!Objects.equals(recentWeightKg, exercise.getWeightKg())) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }

    // 당일 재추천 중량 검사
    public void validateDailyWeight(
            ExerciseRoutineOutputDto exercise,
            AiEligibleExerciseDto candidate,
            List<AiRecentExerciseRecordDto> records,
            AiDailyOriginalExerciseDto original
    ) {
        if (candidate.getBaselineRecordType()
                != ExerciseBaselineType.WEIGHT_REPS_SETS) {
            return;
        }

        // 기존 운동을 유지했다면 중량도 그대로 유지해야 합니다.
        if (exercise.getExerciseCode().equals(original.getExerciseCode())) {
            if (!Objects.equals(
                    original.getWeightKg(),
                    exercise.getWeightKg()
            )) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        } else {
            // 새로운 운동으로 치환한 경우 최근 기준 기록과 비교합니다.
            validateWeeklyWeight(exercise, candidate, records);
        }
    }
}
