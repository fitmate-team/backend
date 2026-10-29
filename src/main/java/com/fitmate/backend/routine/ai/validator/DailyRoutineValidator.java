package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.common.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.daily.AiDailyOriginalExerciseDto;
import com.fitmate.backend.routine.ai.dto.daily.DailyRoutineInputDto;
import com.fitmate.backend.routine.ai.dto.daily.DailyRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.common.ExerciseRoutineOutputDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyRoutineValidator {
    private final ExercisePrescriptionValidator prescriptionValidator;

    public void validate(DailyRoutineInputDto input, DailyRoutineOutputDto output) {

        // AI 응답 및 운동 목록 검사
        if (output == null || output.getMainTarget() == null || output.getReplacements() == null ||
                output.getReplacements().isEmpty() || output.getReplacements().contains(null)) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 예상 운동 시간 검사
        if (output.getEstimatedMinutes() == null || output.getEstimatedMinutes() <= 0 ||
                output.getEstimatedMinutes() > input.getUserProfile().getSessionMinutes()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        List<AiDailyOriginalExerciseDto> originals = input.getOriginalExercises();
        List<DailyRoutineOutputDto.ReplacementDto> replacements = output.getReplacements();

        // 기존 운동과 추천 운동 개수 검사
        if (originals == null || originals.isEmpty() || originals.size() != replacements.size()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        for (int i = 0; i < originals.size(); i++) {
            AiDailyOriginalExerciseDto original = originals.get(i);
            DailyRoutineOutputDto.ReplacementDto replacement = replacements.get(i);

            // 기존 운동 코드와 순서 검사
            if (!original.getExerciseCode().equals(replacement.getOriginalExerciseCode())) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            ExerciseRoutineOutputDto exercise = replacement.getReplacementExercise();

            if (exercise == null || exercise.getExerciseCode() == null ||
                    exercise.getExerciseCode().isBlank() || exercise.getNameKo() == null ||
                    exercise.getNameKo().isBlank()) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            // 기존 운동이 새 환경에서도 가능하면 반드시 유지
            boolean originalEligible = input.getEligibleExercises()
                    .stream()
                    .anyMatch(e -> e.getExerciseCode().equals(original.getExerciseCode()));

            if (originalEligible &&
                    !original.getExerciseCode().equals(exercise.getExerciseCode())) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            // 운동별 처방 검사
            AiEligibleExerciseDto candidate = input.getEligibleExercises()
                    .stream()
                    .filter(e -> e.getExerciseCode().equals(exercise.getExerciseCode()) &&
                            e.getNameKo().equals(exercise.getNameKo()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE));

            prescriptionValidator.validateFormat(exercise, candidate);
            prescriptionValidator.validateDailyWeight(exercise,
                                                      candidate,
                                                      input.getRecentExerciseRecords(),
                                                      original);

        }
    }
}