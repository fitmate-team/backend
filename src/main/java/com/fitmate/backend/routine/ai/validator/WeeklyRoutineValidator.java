package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.input.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.input.WeeklyRoutineInputDto;
import com.fitmate.backend.routine.ai.dto.output.WeeklyRoutineDayOutputDto;
import com.fitmate.backend.routine.ai.dto.output.ExerciseRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.output.WeeklyRoutineOutputDto;
import com.fitmate.backend.routine.domain.MainTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class WeeklyRoutineValidator {
    private final ExercisePrescriptionValidator prescriptionValidator;

    public void validate(WeeklyRoutineInputDto generationInputDto,
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
                                     .map(WeeklyRoutineDayOutputDto::getDay)
                                     .toList())) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 동일한 요일이 중복되지 않았는지
        if (routineOutputDto.getWeeklyPlan()
                .stream()
                .map(WeeklyRoutineDayOutputDto::getDay)
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

        // 각 운동일과 운동의 필수 필드가 null이거나 비어 있지 않은지
        if (routineOutputDto.getWeeklyPlan()
                .stream()
                .anyMatch(day -> day.getMainTarget() == null ||
                        day.getMainTarget() == MainTarget.CORE || day.getFocus() == null ||
                        day.getFocus().isBlank() || day.getDailyReason() == null ||
                        day.getDailyReason().isBlank() || day.getExercises() == null ||
                        day.getExercises().isEmpty() || day.getExercises().contains(null) ||
                        day.getExercises()
                                .stream()
                                .anyMatch(exercise -> exercise.getExerciseCode() == null ||
                                        exercise.getExerciseCode().isBlank() ||
                                        exercise.getNameKo() == null ||
                                        exercise.getNameKo().isBlank()))) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 추천 이유 3개의 필수 필드가 null이거나 비어 있지 않은지
        WeeklyRoutineOutputDto.RecommendationReasons recommendationReasons =
                routineOutputDto.getRecommendationReason();

        if (recommendationReasons == null || recommendationReasons.getPrimaryGoalReason() == null ||
                recommendationReasons.getSecondaryGoalReason() == null ||
                recommendationReasons.getScheduleReason() == null ||
                Stream.of(recommendationReasons.getPrimaryGoalReason(),
                          recommendationReasons.getSecondaryGoalReason(),
                          recommendationReasons.getScheduleReason())
                        .anyMatch(reason -> reason.getTitle() == null ||
                                reason.getTitle().isBlank() || reason.getReason() == null ||
                                reason.getReason().isBlank())) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 추천 운동이 eligibleExercises에 존재하고 운동 코드와 이름이 일치하는지
        if (routineOutputDto.getWeeklyPlan()
                .stream()
                .flatMap(e -> e.getExercises().stream())
                .anyMatch(ex -> generationInputDto.getEligibleExercises()
                        .stream()
                        .noneMatch(candidate -> candidate.getExerciseCode()
                                .equals(ex.getExerciseCode()) &&
                                candidate.getNameKo().equals(ex.getNameKo())))) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // CARDIO를 제외한 동일 운동이 주간 전체에서 중복되지 않는지
        List<String> cardioCodes = generationInputDto.getEligibleExercises()
                .stream()
                .filter(e -> e.getExerciseType().equals(ExerciseType.CARDIO))
                .map(s -> s.getExerciseCode())
                .toList();

        if (routineOutputDto.getWeeklyPlan()
                .stream()
                .flatMap(e -> e.getExercises().stream())
                .filter(k -> !cardioCodes.contains(k.getExerciseCode()))
                .map(exercise -> exercise.getExerciseCode())
                .distinct() // 원소 추출 후 중복 제거
                .count() != routineOutputDto.getWeeklyPlan()
                .stream()
                .flatMap(e -> e.getExercises().stream())
                .filter(k -> !cardioCodes.contains(k.getExerciseCode()))
                .count()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 운동 기록 유형에 맞게 세트, 반복 횟수, 시간, 중량, 휴식값이 설정됐는지
        List<ExerciseRoutineOutputDto> exercises = routineOutputDto.getWeeklyPlan()
                .stream()
                .flatMap(day -> day.getExercises().stream())
                .toList();

        // 운동별 처방 검사
        for (ExerciseRoutineOutputDto exercise : exercises) {
            AiEligibleExerciseDto candidate = generationInputDto.getEligibleExercises()
                    .stream()
                    .filter(v -> v.getExerciseCode().equals(exercise.getExerciseCode()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE));

            prescriptionValidator.validateFormat(exercise, candidate);
            prescriptionValidator.validateWeeklyWeight(exercise,
                                                       candidate,
                                                       generationInputDto.getRecentExerciseRecords());
        }


    }
}
