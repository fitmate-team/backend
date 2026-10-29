package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomSummaryInputDto;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomSummaryOutputDto;
import org.springframework.stereotype.Component;

@Component
public class CustomSummaryValidator {

    public void validate(CustomSummaryInputDto input, CustomSummaryOutputDto output) {

        // 1. AI 입력 운동 목록 검사
        if (input == null || input.getExercises() == null ||
                input.getExercises().isEmpty() ||
                input.getExercises().stream().anyMatch(exercise -> exercise == null)) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 2. AI 응답 필수값 및 예상 운동 시간 검사
        if (output == null || output.getMainTarget() == null ||
                output.getEstimatedMinutes() == null || output.getEstimatedMinutes() <= 0) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }
}