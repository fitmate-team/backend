package com.fitmate.backend.member.controller;

import com.fitmate.backend.member.dto.request.WorkoutEnvironmentUpdateRequestDto;
import com.fitmate.backend.member.dto.request.GymRequestDto;
import com.fitmate.backend.member.dto.request.PrimaryLocationUpdateRequestDto;
import com.fitmate.backend.member.dto.response.WorkoutEnvironmentListResponseDto;
import com.fitmate.backend.member.dto.response.WorkoutEnvironmentResponseDto;
import com.fitmate.backend.member.service.WorkoutEnvironmentService;
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
@RequestMapping("/api/members/workout-environments")
@Tag(name = "운동 환경 API", description = "회원의 운동 장소, 운동 기구, 헬스장 관리")
public class WorkoutEnvironmentController {

    private final WorkoutEnvironmentService workoutEnvironmentService;

    @Operation(summary = "내 운동 환경 조회", description = "현재 주 이용 운동 장소와 등록된 전체 운동 환경을 조회합니다.")
    @GetMapping
    public ResponseEntity<WorkoutEnvironmentListResponseDto> getWorkoutEnvironments(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(workoutEnvironmentService.getWorkoutEnvironments(memberId));
    }

    @Operation(summary = "주 이용 운동 장소 변경", description = "집, 야외, 헬스장 중 주로 이용하는 운동 장소를 변경합니다.")
    @PatchMapping("/primary-location")
    public ResponseEntity<Void> updatePrimaryLocation(@AuthenticationPrincipal Long memberId,
                                                      @Valid @RequestBody PrimaryLocationUpdateRequestDto requestDto) {
        workoutEnvironmentService.updatePrimaryLocation(memberId, requestDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "운동 환경 수정", description = "등록된 운동 환경의 헬스장 이름 또는 운동 기구 목록을 수정합니다.")
    @PatchMapping("/{environmentId}")
    public ResponseEntity<WorkoutEnvironmentResponseDto> updateWorkoutEnvironment(@AuthenticationPrincipal Long memberId,
                                                                                  @PathVariable Long environmentId,
                                                                                  @Valid @RequestBody WorkoutEnvironmentUpdateRequestDto requestDto) {
        return ResponseEntity.ok(workoutEnvironmentService.updateWorkoutEnvironment(memberId,
                                                                                    environmentId,
                                                                                    requestDto));
    }

    @Operation(summary = "헬스장 추가", description = "새로운 헬스장과 해당 헬스장에서 사용할 운동 기구를 등록합니다.")
    @PostMapping("/gyms")
    public ResponseEntity<WorkoutEnvironmentResponseDto> createGym(@AuthenticationPrincipal Long memberId,
                                                                   @Valid @RequestBody GymRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutEnvironmentService.createGym(memberId, requestDto));
    }


}