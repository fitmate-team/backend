package com.fitmate.backend.workout.controller;

import com.fitmate.backend.workout.dto.response.TodayWorkoutResponseDto;
import com.fitmate.backend.workout.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workouts")
@Tag(name = "오늘의 운동 API", description = "오늘의 운동 조회, 오늘의 운동 조건 변경, 운동 기록, 다음 운동 추천")
public class WorkoutController {

    private final WorkoutService workoutService;

    @Operation(summary = "오늘의 운동 조회")
    @GetMapping("/today")
    public TodayWorkoutResponseDto getTodayWorkout(@AuthenticationPrincipal Long memberId) {
        return workoutService.getTodayWorkout(memberId);
    }

}