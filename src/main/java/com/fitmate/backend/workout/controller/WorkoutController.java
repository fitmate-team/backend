package com.fitmate.backend.workout.controller;

import com.fitmate.backend.workout.dto.request.WorkoutFinishRequestDto;
import com.fitmate.backend.workout.dto.request.WorkoutLocationUpdateRequestDto;
import com.fitmate.backend.workout.dto.request.WorkoutLocationValidateRequestDto;
import com.fitmate.backend.workout.dto.request.WorkoutRecordCreateRequestDto;
import com.fitmate.backend.workout.dto.response.TodayWorkoutEnvironmentResponseDto;
import com.fitmate.backend.workout.dto.response.TodayWorkoutResponseDto;
import com.fitmate.backend.workout.dto.response.WorkoutLocationValidateResponseDto;
import com.fitmate.backend.workout.dto.response.WorkoutStartResponseDto;
import com.fitmate.backend.workout.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @Operation(
            summary = "오늘의 운동 환경 선택 화면 조회",
            description = "오늘 예정된 운동 환경과 사용자가 등록한 운동 환경 목록을 조회합니다."
    )
    @GetMapping("/today/environments")
    public TodayWorkoutEnvironmentResponseDto getTodayWorkoutEnvironments(
            @AuthenticationPrincipal Long memberId
    ) {
        return workoutService.getTodayWorkoutEnvironments(memberId);
    }

    @Operation(
            summary = "운동 환경 변경 시 루틴 재추천 필요 여부 검사",
            description = "선택한 운동 환경의 기구 및 장소 조건을 검사하여 루틴 재추천 필요 여부를 반환합니다."
    )
    @PostMapping("/today/location/validate")
    public WorkoutLocationValidateResponseDto validateWorkoutLocation(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody WorkoutLocationValidateRequestDto request
    ) {
        return workoutService.validateWorkoutLocation(
                memberId,
                request.getWorkoutEnvironmentId()
        );
    }

    @Operation(
            summary = "오늘의 운동 환경 변경",
            description = "선택한 운동 환경에서 현재 루틴을 수행할 수 있는지 검사한 후 운동 환경을 변경합니다."
    )
    @PatchMapping("/today/location")
    public ResponseEntity<Void> changeWorkoutLocation(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody WorkoutLocationUpdateRequestDto request
    ) {
        workoutService.changeWorkoutLocation(
                memberId,
                request.getWorkoutEnvironmentId()
        );

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "오늘의 운동 시작",
            description = "운동 시작 일시를 저장하고 미종료 상태의 운동 기록을 생성한 뒤 운동 기록 ID를 반환합니다."
    )
    @PostMapping("/today/start")
    public WorkoutStartResponseDto startWorkout(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody WorkoutRecordCreateRequestDto request
    ) {
        return workoutService.startWorkout(memberId, request);
    }

    @Operation(
            summary = "오늘의 운동 종료",
            description = "정상 종료 또는 중도 종료 시 운동별 수행 기록과 계산 난이도를 저장하고, "
                    + "전체 운동 시간, 심박수, 체감 난이도를 반영하여 종료 처리합니다."
    )
    @PostMapping("/today/finish")
    public ResponseEntity<Void> finishWorkout(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody WorkoutFinishRequestDto request
    ) {
        workoutService.finishWorkout(memberId, request);

        return ResponseEntity.noContent().build();
    }
}