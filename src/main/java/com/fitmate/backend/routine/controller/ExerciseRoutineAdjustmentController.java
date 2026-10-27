package com.fitmate.backend.routine.controller;

import com.fitmate.backend.routine.ai.service.ExerciseAdjustmentAiService;
import com.fitmate.backend.routine.dto.request.ExerciseAdjustmentApplyRequestDto;
import com.fitmate.backend.routine.dto.response.ExerciseAdjustmentResponseDto;
import com.fitmate.backend.routine.service.ExerciseRoutineAdjustmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routine/adjustments")
@Tag(
        name = "운동별 루틴 조정 API",
        description = "운동 결과에 따른 조정값 추천과 적용 및 삭제"
)
public class ExerciseRoutineAdjustmentController {

    private final ExerciseAdjustmentAiService exerciseAdjustmentAiService;
    private final ExerciseRoutineAdjustmentService exerciseRoutineAdjustmentService;

    @Operation(
            summary = "운동별 AI 조정 추천",
            description = "종료된 운동 기록을 바탕으로 운동별 조정값을 추천합니다."
    )
    @PostMapping("/recommendations/{workoutRecordId}")
    public ResponseEntity<ExerciseAdjustmentResponseDto> recommendAdjustments(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long workoutRecordId
    ) {
        ExerciseAdjustmentResponseDto response =
                exerciseAdjustmentAiService.recommendAdjustments(
                        memberId,
                        workoutRecordId
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "운동별 조정 추천값 적용",
            description = "선택한 운동의 조정값을 저장하거나 기존 조정값을 수정합니다."
    )
    @PutMapping("/{workoutRecordId}/exercises/{exerciseId}")
    public ResponseEntity<Void> applyAdjustment(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long workoutRecordId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody ExerciseAdjustmentApplyRequestDto request
    ) {
        exerciseRoutineAdjustmentService.applyAdjustment(
                memberId,
                workoutRecordId,
                exerciseId,
                request
        );

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "운동별 기존 조정값 삭제",
            description = "선택한 운동의 기존 조정값을 삭제하고 이후 루틴에서 AI 추천값을 사용합니다."
    )
    @DeleteMapping("/{workoutRecordId}/exercises/{exerciseId}")
    public ResponseEntity<Void> deleteAdjustment(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long workoutRecordId,
            @PathVariable Long exerciseId
    ) {
        exerciseRoutineAdjustmentService.deleteAdjustment(
                memberId,
                workoutRecordId,
                exerciseId
        );

        return ResponseEntity.noContent().build();
    }
}