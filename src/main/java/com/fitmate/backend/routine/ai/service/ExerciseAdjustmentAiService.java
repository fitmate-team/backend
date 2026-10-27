package com.fitmate.backend.routine.ai.service;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.client.OpenAiClient;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentInputDto;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentInputItemDto;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentOutputDto;
import com.fitmate.backend.routine.ai.prompt.AiPromptBuilder;
import com.fitmate.backend.routine.ai.validator.ExerciseAdjustmentValidator;
import com.fitmate.backend.workout.domain.CalculatedDifficulty;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import com.fitmate.backend.workout.repository.WorkoutExerciseRecordRepository;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitmate.backend.routine.ai.dto.adjustment.ExerciseAdjustmentOutputItemDto;
import com.fitmate.backend.routine.dto.response.ExerciseAdjustmentResponseDto;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseAdjustmentAiService {

    private final WorkoutRecordRepository workoutRecordRepository;
    private final WorkoutExerciseRecordRepository workoutExerciseRecordRepository;

    private final AiPromptBuilder aiPromptBuilder;
    private final OpenAiClient openAiClient;
    private final ExerciseAdjustmentValidator exerciseAdjustmentValidator;

    /**
     * 종료된 운동 기록을 바탕으로 운동별 조정값을 추천한다.
     */
    /**
     * 종료된 운동 기록을 바탕으로 운동별 조정값을 추천한다.
     */
    public ExerciseAdjustmentResponseDto recommendAdjustments(
            Long memberId,
            Long workoutRecordId
    ) {
        // 로그인한 회원의 종료된 운동 기록인지 확인한다.
        workoutRecordRepository.findFinishedRecordByIdAndMemberId(
                workoutRecordId,
                memberId
        ).orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_RECORD_NOT_FOUND));

        // 운동별 수행 기록을 루틴에 지정된 순서대로 조회한다.
        List<WorkoutExerciseRecord> workoutExerciseRecords =
                workoutExerciseRecordRepository
                        .findByWorkoutRecord_IdOrderByExerciseRoutine_ExerciseOrderAsc(
                                workoutRecordId
                        );

        // EASY·HARD 운동만 AI 조정 추천 대상으로 변환한다.
        List<ExerciseAdjustmentInputItemDto> exerciseRecords =
                workoutExerciseRecords.stream()
                        .filter(this::isAdjustmentTarget)
                        .map(ExerciseAdjustmentInputItemDto::from)
                        .toList();

        // 추천 대상이 없으면 AI를 호출하지 않고 빈 목록을 반환한다.
        if (exerciseRecords.isEmpty()) {
            return ExerciseAdjustmentResponseDto.of(workoutRecordId, List.of());
        }

        ExerciseAdjustmentInputDto input =
                ExerciseAdjustmentInputDto.of(exerciseRecords);

        // 입력 운동 기록을 프롬프트에 넣어 AI를 호출한다.
        String prompt = aiPromptBuilder.build(input);

        ExerciseAdjustmentOutputDto output = openAiClient.generate(
                prompt,
                ExerciseAdjustmentOutputDto.class
        );

        // AI 응답의 운동 목록, 기록 방식, 조정 방향을 검증한다.
        exerciseAdjustmentValidator.validate(input, output);

        // AI 추천 결과를 운동 ID로 조회할 수 있도록 정리한다.
        Map<Long, ExerciseAdjustmentOutputItemDto> outputByExerciseId =
                output.getExercises().stream()
                        .collect(Collectors.toMap(
                                ExerciseAdjustmentOutputItemDto::getExerciseId,
                                Function.identity()
                        ));

        // 기존 루틴 순서에 맞춰 현재 값과 AI 추천값을 응답 DTO로 변환한다.
        List<ExerciseAdjustmentResponseDto.ExerciseAdjustmentItem> exercises =
                exerciseRecords.stream()
                        .map(record -> ExerciseAdjustmentResponseDto.ExerciseAdjustmentItem.of(
                                record,
                                outputByExerciseId.get(record.getExerciseId())
                        ))
                        .toList();

        return ExerciseAdjustmentResponseDto.of(workoutRecordId, exercises);
    }

    /**
     * EASY 또는 HARD 운동만 조정 추천 대상으로 판단한다.
     */
    private boolean isAdjustmentTarget(WorkoutExerciseRecord exerciseRecord) {
        CalculatedDifficulty difficulty = exerciseRecord.getCalculatedDifficulty();

        return difficulty == CalculatedDifficulty.EASY
                || difficulty == CalculatedDifficulty.HARD;
    }
}