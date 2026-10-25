package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.common.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomExerciseInputDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomExerciseOutputDto;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomExerciseValidator {

    public void validate(CustomExerciseInputDto input, CustomExerciseOutputDto output) {

        // AI 응답 및 추천 운동 개수 검사 (3~4개)
        if (output == null || output.getExerciseCodes() == null ||
                output.getExerciseCodes().size() < 3 || output.getExerciseCodes().size() > 4) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        List<String> exerciseCodes = output.getExerciseCodes();

        // 운동 코드가 null이거나 빈 문자열인지 검사
        if (exerciseCodes.stream().anyMatch(code -> code == null || code.isBlank())) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 동일한 운동 코드가 중복되지 않았는지 검사
        if (new HashSet<>(exerciseCodes).size() != exerciseCodes.size()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 백엔드가 허용한 추천 후보 운동 코드 목록
        Set<String> eligibleCodes = input.getEligibleExercises()
                .stream()
                .map(AiEligibleExerciseDto::getExerciseCode)
                .collect(Collectors.toSet());

        // 추천 운동이 모두 허용된 후보 목록에 있는지 검사
        if (!eligibleCodes.containsAll(exerciseCodes)) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }
}
