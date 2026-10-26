package com.fitmate.backend.routine.ai.controller;

import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomAiExerciseRequestDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomExerciseOutputDto;
import com.fitmate.backend.routine.ai.dto.custom.prescription.CustomAiPrescriptionRequestDto;
import com.fitmate.backend.routine.ai.dto.custom.prescription.CustomPrescriptionResponseDto;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomAiSummaryRequestDto;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomSummaryOutputDto;
import com.fitmate.backend.routine.ai.dto.daily.DailyAiRoutineRequestDto;
import com.fitmate.backend.routine.ai.dto.daily.DailyRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.weekly.WeeklyAiRoutineRequestDto;
import com.fitmate.backend.routine.ai.dto.weekly.WeeklyRoutineOutputDto;
import com.fitmate.backend.routine.ai.service.CustomRoutineAiService;
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
@Tag(name = "루틴 AI API", description = "AI 기반 운동 루틴 생성 및 추천")
public class AiRoutineController {

    private final WeeklyRoutineAiService weeklyRoutineAiService;
    private final DailyRoutineAiService dailyRoutineAiService;
    private final CustomRoutineAiService customRoutineAiService;

    @Operation(summary = "주간 루틴 AI 생성", description = "선택한 요일과 환경에 맞는 주간 루틴을 생성합니다.")
    @PostMapping("/weekly-routines")
    public ResponseEntity<WeeklyRoutineOutputDto> previewWeeklyRoutine(@AuthenticationPrincipal Long memberId,
                                                                       @Valid @RequestBody WeeklyAiRoutineRequestDto requestDto) {
        return ResponseEntity.ok(weeklyRoutineAiService.generateWeeklyRoutine(memberId,
                                                                              requestDto));
    }

    @Operation(summary = "당일 루틴 AI 재추천", description = "변경한 환경과 시간에 맞춰 당일 루틴을 재추천합니다.")
    @PostMapping("/daily-routines")
    public ResponseEntity<DailyRoutineOutputDto> previewDailyRoutine(@AuthenticationPrincipal Long memberId,
                                                                     @Valid @RequestBody DailyAiRoutineRequestDto requestDto) {
        return ResponseEntity.ok(dailyRoutineAiService.generateDailyRoutine(memberId, requestDto));
    }

    @Operation(summary = "직접 만들기 - 운동 종목 AI 추천", description = "선택한 환경에 맞는 운동 3~4개를 추천합니다.")
    @PostMapping("/custom-routines/exercises")
    public ResponseEntity<CustomExerciseOutputDto> previewCustomExercises(@AuthenticationPrincipal Long memberId,
                                                                          @Valid @RequestBody CustomAiExerciseRequestDto requestDto) {
        return ResponseEntity.ok(customRoutineAiService.generateExerciseRecommendations(memberId,
                                                                                        requestDto));
    }

    @Operation(summary = "직접 만들기 - 처방값 AI 추천", description = "선택한 운동들의 실제 수행 기록 또는 Baseline을 참고하여" +
            " 처방값을 추천합니다.")
    @PostMapping("/custom-routines/prescription")
    public ResponseEntity<CustomPrescriptionResponseDto> previewCustomPrescription(@AuthenticationPrincipal Long memberId,
                                                                                   @Valid @RequestBody CustomAiPrescriptionRequestDto requestDto) {
        return ResponseEntity.ok(customRoutineAiService.generatePrescriptionRecommendations(memberId,
                                                                                            requestDto));
    }

    @Operation(summary = "직접 만들기 - 운동 이름 AI 추천", description = "")
    @PostMapping("/custom-routines/summary")
    public ResponseEntity<CustomSummaryOutputDto> previewCustomPrescription(@AuthenticationPrincipal Long memberId,
                                                                            @Valid @RequestBody CustomAiSummaryRequestDto requestDto) {
        return ResponseEntity.ok(customRoutineAiService.generateSummary(memberId, requestDto));
    }
}
