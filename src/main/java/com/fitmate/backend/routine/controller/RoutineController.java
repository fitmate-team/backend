package com.fitmate.backend.routine.controller;

import com.fitmate.backend.routine.dto.request.RoutineWeightUpdateRequestDto;
import com.fitmate.backend.routine.service.AiRoutineService;
import com.fitmate.backend.routine.service.RoutineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routine")
@Tag(name = "루틴 API", description = "중량 설정")
public class RoutineController {

    private final RoutineService routineService;
    private final AiRoutineService aiRoutineService;

    @Operation(summary = "오늘의 운동 중량 설정", description = "AI가 중량을 추천하지 못한 운동에 대해 사용자가 직접 중량을 설정합니다.")
    @PatchMapping("/weights")
    public ResponseEntity<Void> updateRoutineWeights(@AuthenticationPrincipal Long memberId,
                                                     @Valid @RequestBody RoutineWeightUpdateRequestDto request) {
        routineService.updateRoutineWeights(memberId, request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ai-prompt")
    public ResponseEntity<String> getAiPrompt(@AuthenticationPrincipal Long memberId,
                                         @RequestParam Long environmentId,
                                         @RequestParam LocalDate weekStartDate) {

        return ResponseEntity.ok(aiRoutineService.createAiPrompt(memberId,
                                                                 environmentId,
                                                                 weekStartDate));

    }
}
