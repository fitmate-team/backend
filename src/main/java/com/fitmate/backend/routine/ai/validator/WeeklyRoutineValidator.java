package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.input.WeeklyGenerationInputDto;
import com.fitmate.backend.routine.ai.dto.output.DailyRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.output.WeeklyRoutineOutputDto;

public class WeeklyRoutineValidator {
    public void validate(WeeklyGenerationInputDto generationInputDto,
                         WeeklyRoutineOutputDto routineOutputDto) {

        // AI가 생성한 weeklyPlan이 비어 있거나 null인지
        if (routineOutputDto == null || routineOutputDto.getWeeklyPlan() == null ||
                routineOutputDto.getWeeklyPlan().contains(null) ||
                routineOutputDto.getWeeklyPlan().isEmpty()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 생성된 운동일 수가 요청한 weeklyFrequency와 같은지
        if (routineOutputDto.getWeeklyPlan().size() !=
                generationInputDto.getUserProfile().getWeeklyFrequency()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 생성된 요일이 모두 요청한 availableDays에 포함되는지
        if (!generationInputDto.getUserProfile()
                .getAvailableDays()
                .containsAll(routineOutputDto.getWeeklyPlan()
                                     .stream()
                                     .map(DailyRoutineOutputDto::getDay)
                                     .toList())) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 동일한 요일이 중복되지 않았는지
        if (routineOutputDto.getWeeklyPlan()
                .stream()
                .map(DailyRoutineOutputDto::getDay)
                .distinct()
                .count() != routineOutputDto.getWeeklyPlan().size()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 각 운동일의 estimatedMinutes가 0보다 크고 sessionMinutes 이하인지
        if (routineOutputDto.getWeeklyPlan()
                .stream()
                .anyMatch(e -> e.getEstimatedMinutes() == null || e.getEstimatedMinutes() <= 0 ||
                        e.getEstimatedMinutes() >
                                generationInputDto.getUserProfile().getSessionMinutes())) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

    }
}
