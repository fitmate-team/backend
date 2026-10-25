package com.fitmate.backend.routine.ai.controller;

import com.fitmate.backend.routine.ai.dto.output.DailyRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.output.WeeklyRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.request.DailyAiRoutineRequestDto;
import com.fitmate.backend.routine.ai.dto.request.WeeklyAiRoutineRequestDto;
import com.fitmate.backend.routine.ai.service.DailyRoutineAiService;
import com.fitmate.backend.routine.ai.service.WeeklyRoutineAiService;
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
@Tag(name = "루틴 AI API", description = "루틴 생성 관련 API")
public class AiRoutineController {
    private final WeeklyRoutineAiService weeklyRoutineAiService;
    private final DailyRoutineAiService dailyRoutineAiService;

    @Operation(summary = "주간 루틴 생성 프롬포트")
    @PostMapping("/weekly-routines/prompt")
    public ResponseEntity<String> getWeeklyAiPrompt(@AuthenticationPrincipal Long memberId,
                                                    @Valid @RequestBody WeeklyAiRoutineRequestDto requestDto) {

        return ResponseEntity.ok(weeklyRoutineAiService.createAiPrompt(memberId, requestDto));
    }

    @Operation(summary = "주간 루틴 생성 (프리뷰)")
    @PostMapping("/weekly-routines/preview")
    public ResponseEntity<WeeklyRoutineOutputDto> generateWeeklyRoutine(@AuthenticationPrincipal Long memberId,
                                                                        @Valid @RequestBody WeeklyAiRoutineRequestDto requestDto) {
        return ResponseEntity.ok(weeklyRoutineAiService.generateWeeklyRoutine(memberId, requestDto));
    }

    @Operation(summary = "당일 루틴 생성 프롬포트")
    @PostMapping("/daily-routines/prompt")
    public ResponseEntity<String> getDailyPrompt(@AuthenticationPrincipal Long memberId,
                                                 @Valid @RequestBody DailyAiRoutineRequestDto requestDto) {

        return ResponseEntity.ok(dailyRoutineAiService.createAiPrompt(memberId, requestDto));
    }

    @Operation(summary = "당일 루틴 생성 (프리뷰)")
    @PostMapping("/daily-routines/preview")
    public ResponseEntity<DailyRoutineOutputDto> generateDailyRoutine(@AuthenticationPrincipal Long memberId,
                                                                      @Valid @RequestBody DailyAiRoutineRequestDto requestDto) {
        return ResponseEntity.ok(dailyRoutineAiService.generateDailyRoutine(memberId, requestDto));
    }
}
