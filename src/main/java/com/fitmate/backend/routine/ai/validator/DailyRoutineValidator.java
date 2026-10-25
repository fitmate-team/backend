package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.input.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.input.AiDailyOriginalExerciseDto;
import com.fitmate.backend.routine.ai.dto.input.DailyRoutineInputDto;
import com.fitmate.backend.routine.ai.dto.output.DailyRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.output.ExerciseRoutineOutputDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyRoutineValidator {

    public void validate(DailyRoutineInputDto input,
                         DailyRoutineOutputDto output) {

        // AI 응답 및 운동 목록 검사
        if (output == null || output.getMainTarget() == null ||
                output.getReplacements() == null ||
                output.getReplacements().isEmpty() ||
                output.getReplacements().contains(null)) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 예상 운동 시간 검사
        if (output.getEstimatedMinutes() == null ||
                output.getEstimatedMinutes() <= 0 ||
                output.getEstimatedMinutes() > input.getUserProfile().getSessionMinutes()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        List<AiDailyOriginalExerciseDto> originals = input.getOriginalExercises();
        List<DailyRoutineOutputDto.ReplacementDto> replacements =
                output.getReplacements();

        // 기존 운동과 추천 운동 개수 검사
        if (originals == null || originals.isEmpty() ||
                originals.size() != replacements.size()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        for (int i = 0; i < originals.size(); i++) {
            AiDailyOriginalExerciseDto original = originals.get(i);
            DailyRoutineOutputDto.ReplacementDto replacement =
                    replacements.get(i);

            // 기존 운동 코드와 순서 검사
            if (!original.getExerciseCode().equals(
                    replacement.getOriginalExerciseCode())) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            ExerciseRoutineOutputDto exercise = replacement.getReplacementExercise();

            if (exercise == null ||
                    exercise.getExerciseCode() == null ||
                    exercise.getExerciseCode().isBlank() ||
                    exercise.getNameKo() == null ||
                    exercise.getNameKo().isBlank()) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            // 추천 가능한 운동 코드 및 이름 검사
            boolean eligible = input.getEligibleExercises().stream()
                    .anyMatch((AiEligibleExerciseDto candidate) ->
                                      candidate.getExerciseCode().equals(exercise.getExerciseCode()) &&
                                              candidate.getNameKo().equals(exercise.getNameKo()));

            if (!eligible) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }
        }
    }
}