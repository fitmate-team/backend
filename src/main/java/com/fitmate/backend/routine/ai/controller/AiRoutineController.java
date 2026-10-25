package com.fitmate.backend.routine.ai.controller;

import com.fitmate.backend.routine.ai.dto.output.WeeklyRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.request.DailyRoutineRequestDto;
import com.fitmate.backend.routine.ai.dto.request.WeeklyRoutineRequestDto;
import com.fitmate.backend.routine.ai.service.DailyRoutineService;
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
@Tag(name = "루틴 AI API", description = "루틴 생성 관련 API")
public class AiRoutineController {
    private final WeeklyRoutineService weeklyRoutineService;
    private final DailyRoutineService dailyRoutineService;

    @Operation(summary = "주간 루틴 생성 프롬포트")
    @PostMapping("/weekly-routines/prompt")
    public ResponseEntity<String> getWeeklyAiPrompt(@AuthenticationPrincipal Long memberId,
                                                    @Valid @RequestBody WeeklyRoutineRequestDto requestDto) {

        return ResponseEntity.ok(weeklyRoutineService.createAiPrompt(memberId, requestDto));
    }

    @Operation(summary = "주간 루틴 생성 (프리뷰)")
    @PostMapping("/weekly-routines/preview")
    public ResponseEntity<WeeklyRoutineOutputDto> generateWeeklyRoutine(@AuthenticationPrincipal Long memberId,
                                                                        @Valid @RequestBody WeeklyRoutineRequestDto requestDto) {
        return ResponseEntity.ok(weeklyRoutineService.generateWeeklyRoutine(memberId, requestDto));
    }

    @Operation(summary = "당일 루틴 생성 프롬포트")
    @PostMapping("/daily-routines/prompt")
    public ResponseEntity<String> getDailyPrompt(@AuthenticationPrincipal Long memberId,
                                                 @Valid @RequestBody DailyRoutineRequestDto requestDto) {

        return ResponseEntity.ok(dailyRoutineService.createAiPrompt(memberId, requestDto));
    }

    @Operation(summary = "당일 루틴 생성 (프리뷰)")
    @PostMapping("/daily-routines/preview")
    public ResponseEntity<WeeklyRoutineOutputDto> generateDailyRoutine(@AuthenticationPrincipal Long memberId,
                                                                       @Valid @RequestBody DailyRoutineRequestDto requestDto) {
        return ResponseEntity.ok(dailyRoutineService.generateDailyRoutine(memberId, requestDto));
    }
}
