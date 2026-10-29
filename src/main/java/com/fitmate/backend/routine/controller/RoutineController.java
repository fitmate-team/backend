package com.fitmate.backend.routine.controller;

import com.fitmate.backend.routine.dto.request.CustomRoutineSaveRequestDto;
import com.fitmate.backend.routine.dto.request.DailyRoutineSaveRequestDto;
import com.fitmate.backend.routine.dto.request.RoutineWeightUpdateRequestDto;
import com.fitmate.backend.routine.dto.request.WeeklyRoutineSaveRequestDto;
import com.fitmate.backend.routine.dto.response.WeeklyRoutineResponseDto;
import com.fitmate.backend.routine.service.CustomRoutineService;
import com.fitmate.backend.routine.service.DailyRoutineService;
import com.fitmate.backend.routine.service.RoutineWeightService;
import com.fitmate.backend.routine.service.WeeklyRoutineService;
import com.fitmate.backend.workout.dto.response.TodayWorkoutResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routine")
@Tag(name = "루틴 API", description = "루틴 저장 및 조회, 중량 설정")
public class RoutineController {

    private final RoutineWeightService routineWeightService;
    private final WeeklyRoutineService weeklyRoutineService;
    private final DailyRoutineService dailyRoutineService;
    private final CustomRoutineService customRoutineService;

    @Operation(summary = "오늘의 운동 중량 설정", description = "AI가 중량을 추천하지 못한 운동에 대해 사용자가 직접 중량을 설정합니다.")
    @PatchMapping("/weights")
    public ResponseEntity<Void> updateRoutineWeights(@AuthenticationPrincipal Long memberId,
                                                     @Valid @RequestBody RoutineWeightUpdateRequestDto request) {
        routineWeightService.updateRoutineWeights(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "주간 루틴 저장", description = "AI가 생성한 주간 운동 루틴을 사용자의 이번 주 루틴으로 저장합니다.")
    @PostMapping("/weekly")
    public ResponseEntity<Void> saveWeeklyRoutine(@AuthenticationPrincipal Long memberId,
                                                  @Valid @RequestBody WeeklyRoutineSaveRequestDto request) {
        weeklyRoutineService.saveRoutine(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "주간 루틴 조회", description = "로그인한 사용자의 이번 주 주간 운동 루틴과 날짜별 운동 목록을 " +
            "조회합니다.")
    @GetMapping("/weekly")
    public ResponseEntity<WeeklyRoutineResponseDto> getWeeklyRoutine(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(weeklyRoutineService.getRoutine(memberId));
    }

    @Operation(summary = "당일 루틴 재추천 저장", description = "AI가 재추천한 운동 목록을 기존 오늘의 운동 루틴에 반영합니다.")
    @PostMapping("/daily")
    public ResponseEntity<Void> saveDailyRoutine(@AuthenticationPrincipal Long memberId,
                                                 @Valid @RequestBody DailyRoutineSaveRequestDto request) {
        dailyRoutineService.saveRoutine(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "AI와 함께 만든 주간 루틴 저장", description = "사용자가 최종 확정한 요일별 운동 환경과 운동 처방을 이번 주 " +
            "루틴으로 저장합니다.")
    @PostMapping("/custom")
    public ResponseEntity<Void> saveCustomRoutine(@AuthenticationPrincipal Long memberId,
                                                  @Valid @RequestBody CustomRoutineSaveRequestDto request) {
        customRoutineService.saveRoutine(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
