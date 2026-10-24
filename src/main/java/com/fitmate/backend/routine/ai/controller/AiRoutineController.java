package com.fitmate.backend.routine.ai.controller;

import com.fitmate.backend.routine.ai.dto.output.WeeklyRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.request.WeeklyGenerationRequestDto;
import com.fitmate.backend.routine.ai.service.WeeklyRoutineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@Tag(name = "주간 루틴 AI API", description = "주간 루틴 생성 관련 API")
public class AiRoutineController {
    private final WeeklyRoutineService weeklyRoutineService;

    @Operation(summary = "주간 루틴 생성 프롬포트")
    @PostMapping("/prompt")
    public ResponseEntity<String> getAiPrompt(@AuthenticationPrincipal Long memberId,
                                              @Valid @RequestBody WeeklyGenerationRequestDto requestDto) {

        return ResponseEntity.ok(weeklyRoutineService.createAiPrompt(memberId, requestDto));
    }

    @Operation(summary = "주간 루틴 생성 (프리뷰)")
    @PostMapping("/weekly-routines/preview")
    public ResponseEntity<WeeklyRoutineOutputDto> generateWeeklyRoutine(@AuthenticationPrincipal Long memberId,
                                                                        @Valid @RequestBody WeeklyGenerationRequestDto requestDto) {
        return ResponseEntity.ok(weeklyRoutineService.generateWeeklyRoutine(memberId, requestDto));
    }
}
