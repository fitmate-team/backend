package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.routine.ai.client.OpenAiClient;
import com.fitmate.backend.routine.ai.dto.custom.summary.AiCustomSummaryExerciseDto;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomSummaryInputDto;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomSummaryOutputDto;
import com.fitmate.backend.routine.ai.prompt.AiPromptBuilder;
import com.fitmate.backend.routine.ai.validator.CustomSummaryValidator;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.dto.request.DailyRoutineExerciseUpdateRequestDto;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyRoutineExerciseUpdateService {

    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final WorkoutRecordRepository workoutRecordRepository;
    private final ExerciseRepository exerciseRepository;
    private final AiPromptBuilder aiPromptBuilder;
    private final OpenAiClient openAiClient;
    private final CustomSummaryValidator customSummaryValidator;

    /**
     * 오늘의 운동 목록 전체를 교체하고 주요 운동 부위와 예상 시간을 갱신합니다.
     * AI 호출 또는 응답 검증이 실패하면 운동 목록 변경도 함께 롤백됩니다.
     */
    @Transactional
    public void updateExercises(
            Long memberId,
            Long dailyRoutineId,
            DailyRoutineExerciseUpdateRequestDto request
    ) {
        // 데일리 루틴을 조회하면서 로그인한 회원의 소유인지 확인합니다.
        DailyRoutine dailyRoutine = dailyRoutineRepository
                .findByIdAndWeeklyRoutine_Member_Id(dailyRoutineId, memberId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.DAILY_ROUTINE_NOT_FOUND)
                );

        // 한국 시간 기준으로 오늘의 루틴만 수정할 수 있습니다.
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        if (!dailyRoutine.getRoutineDate().equals(today)) {
            throw new CustomException(ErrorCode.INVALID_DAILY_ROUTINE_SAVE);
        }

        // 종료 여부와 관계없이 운동 기록이 존재하면 이미 시작한 운동입니다.
        if (workoutRecordRepository.existsByDailyRoutine_Id(dailyRoutineId)) {
            throw new CustomException(ErrorCode.WORKOUT_ALREADY_STARTED);
        }

        // 요청 운동의 코드, 환경 적합성 및 처방값을 모두 검증합니다.
        List<DailyRoutineExerciseUpdateRequestDto.ExerciseDto> requestedExercises =
                request.getExercises();
        Map<String, Exercise> exerciseMap = validateExercises(
                requestedExercises,
                dailyRoutine.getWorkoutEnvironment()
        );

        // 새로 저장할 운동 목록과 Summary AI에 전달할 운동 목록을 준비합니다.
        List<ExerciseRoutine> replacements = new ArrayList<>();
        List<AiCustomSummaryExerciseDto> summaryExercises = new ArrayList<>();

        // 요청 배열의 순서를 유지하여 운동별 저장 데이터와 AI 입력을 구성합니다.
        for (int i = 0; i < requestedExercises.size(); i++) {
            DailyRoutineExerciseUpdateRequestDto.ExerciseDto dto =
                    requestedExercises.get(i);
            Exercise exercise = exerciseMap.get(dto.getExerciseCode());

            // 기존 데일리 루틴에 연결할 새 배정 운동을 생성합니다.
            replacements.add(
                    ExerciseRoutine.builder()
                            .dailyRoutine(dailyRoutine)
                            .exercise(exercise)
                            .exerciseOrder(i + 1)
                            .weight(dto.getWeightKg())
                            .reps(dto.getReps())
                            .setCount(dto.getSets())
                            .durationSeconds(dto.getDurationSeconds())
                            .restSeconds(dto.getRestSeconds())
                            .build()
            );

            // 운동 마스터 정보와 사용자가 확정한 처방값으로 AI 입력을 구성합니다.
            summaryExercises.add(
                    new AiCustomSummaryExerciseDto(
                            exercise.getNameKo(),
                            exercise.getExerciseType(),
                            exercise.getPrimaryMuscle(),
                            exercise.getBaselineRecordType(),
                            exercise.getExerciseCode(),
                            dto.getWeightKg(),
                            dto.getReps(),
                            dto.getSets(),
                            dto.getDurationSeconds(),
                            dto.getRestSeconds()
                    )
            );
        }

        // 기존 배정 운동을 일괄 삭제한 뒤 새 목록을 저장합니다.
        exerciseRoutineRepository.deleteAllByDailyRoutineId(dailyRoutineId);
        exerciseRoutineRepository.saveAll(replacements);

        // 기존 Summary 프롬프트로 주요 운동 부위와 전체 예상 시간을 요청합니다.
        CustomSummaryInputDto input =
                CustomSummaryInputDto.of(summaryExercises);
        String prompt = aiPromptBuilder.build(input);
        CustomSummaryOutputDto summary = openAiClient.generate(
                prompt,
                CustomSummaryOutputDto.class
        );

        // AI 응답에 주요 운동 부위와 양수인 예상 시간이 있는지 검증합니다.
        customSummaryValidator.validate(input, summary);

        // 기존 운동 환경은 유지하고 주요 운동 부위와 예상 시간만 갱신합니다.
        dailyRoutine.updateRoutine(
                summary.getMainTarget(),
                dailyRoutine.getWorkoutEnvironment(),
                summary.getEstimatedMinutes()
        );
    }

    /**
     * 운동 코드의 중복·존재 여부, 환경 적합성 및 처방값을 검증합니다.
     */
    private Map<String, Exercise> validateExercises(
            List<DailyRoutineExerciseUpdateRequestDto.ExerciseDto> requestedExercises,
            WorkoutEnvironment environment
    ) {
        // 운동 목록이 없으면 전체 교체를 진행하지 않습니다.
        if (requestedExercises == null || requestedExercises.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_DAILY_ROUTINE_SAVE);
        }

        // 운동 코드의 누락과 중복을 확인하면서 조회할 코드 목록을 구성합니다.
        Set<String> exerciseCodes = new HashSet<>();
        for (DailyRoutineExerciseUpdateRequestDto.ExerciseDto dto
                : requestedExercises) {
            if (dto == null
                    || dto.getExerciseCode() == null
                    || dto.getExerciseCode().isBlank()
                    || !exerciseCodes.add(dto.getExerciseCode())) {
                throw new CustomException(ErrorCode.INVALID_EXERCISE_CODE);
            }
        }

        // 요청한 운동을 일괄 조회하고 운동 코드로 접근할 수 있도록 변환합니다.
        Map<String, Exercise> exerciseMap = exerciseRepository
                .findAllByExerciseCodeIn(exerciseCodes)
                .stream()
                .collect(Collectors.toMap(
                        Exercise::getExerciseCode,
                        Function.identity()
                ));

        // 요청 코드 중 운동 마스터에 존재하지 않는 코드가 있는지 확인합니다.
        if (exerciseMap.size() != exerciseCodes.size()) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_CODE);
        }

        // 각 운동이 기존 환경에서 수행 가능한지 확인하고 처방값을 검증합니다.
        for (DailyRoutineExerciseUpdateRequestDto.ExerciseDto dto
                : requestedExercises) {
            Exercise exercise = exerciseMap.get(dto.getExerciseCode());

            // 운동 장소가 일치하고 필요한 기구를 모두 보유해야 합니다.
            if (!exercise.getAvailableLocations()
                    .contains(environment.getLocationType())
                    || !environment.getEquipment()
                    .containsAll(exercise.getEquipment())) {
                throw new CustomException(
                        ErrorCode.EXERCISE_NOT_AVAILABLE_IN_ENVIRONMENT
                );
            }

            // 운동 기록 유형에 맞는 중량·횟수·세트·시간·휴식인지 확인합니다.
            validatePrescription(exercise, dto);
        }

        // 검증을 통과한 운동 목록을 저장 및 AI 입력 생성에 사용합니다.
        return exerciseMap;
    }

    /**
     * 운동 기록 유형에 맞는 처방 형식인지 검증합니다.
     */
    private void validatePrescription(
            Exercise exercise,
            DailyRoutineExerciseUpdateRequestDto.ExerciseDto dto
    ) {
        // 필수 수치와 선택 수치의 범위 및 운동 유형 정보의 존재 여부를 확인합니다.
        if (dto.getSets() == null
                || dto.getSets() <= 0
                || dto.getRestSeconds() == null
                || dto.getRestSeconds() < 0
                || (dto.getWeightKg() != null
                && dto.getWeightKg() <= 0)
                || (dto.getReps() != null && dto.getReps() <= 0)
                || (dto.getDurationSeconds() != null
                && dto.getDurationSeconds() <= 0)
                || exercise.getBaselineRecordType() == null
                || exercise.getExerciseType() == null) {
            throw new CustomException(
                    ErrorCode.INVALID_CUSTOM_ROUTINE_PRESCRIPTION
            );
        }

        // 반복 운동과 시간 기반 유산소 운동의 처방 필드 조합을 확인합니다.
        boolean invalidPrescription = switch (exercise.getBaselineRecordType()) {
            // 중량 기반 운동은 반복 횟수가 필요하고 수행 시간은 사용하지 않습니다.
            case WEIGHT_REPS_SETS ->
                    dto.getReps() == null
                            || dto.getDurationSeconds() != null
                            || exercise.getExerciseType() == ExerciseType.CARDIO;

            // 맨몸 등 반복 기반 운동은 중량과 수행 시간을 사용하지 않습니다.
            case REPS_SETS ->
                    dto.getReps() == null
                            || dto.getWeightKg() != null
                            || dto.getDurationSeconds() != null
                            || exercise.getExerciseType() == ExerciseType.CARDIO;

            // 유산소는 전체 수행 시간을 받으며 세트는 1, 휴식은 0으로 제한합니다.
            case DURATION ->
                    exercise.getExerciseType() != ExerciseType.CARDIO
                            || dto.getDurationSeconds() == null
                            || dto.getReps() != null
                            || dto.getWeightKg() != null
                            || dto.getSets() != 1
                            || dto.getRestSeconds() != 0;
        };

        // 운동 유형에 맞지 않는 처방이면 저장하지 않습니다.
        if (invalidPrescription) {
            throw new CustomException(
                    ErrorCode.INVALID_CUSTOM_ROUTINE_PRESCRIPTION
            );
        }
    }
}