package com.fitmate.backend.routine.controller;

import com.fitmate.backend.routine.dto.response.DailyRoutineAvailableExerciseResponseDto;
import com.fitmate.backend.routine.service.DailyRoutineExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fitmate.backend.routine.dto.request.DailyRoutineExerciseUpdateRequestDto;
import com.fitmate.backend.routine.service.DailyRoutineExerciseUpdateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-routines")
@Tag(
        name = "데일리 루틴 운동 API",
        description = "데일리 루틴 직접 수정을 위한 운동 조회 API"
)
public class DailyRoutineExerciseController {

    private final DailyRoutineExerciseService dailyRoutineExerciseService;
    private final DailyRoutineExerciseUpdateService dailyRoutineExerciseUpdateService;

    /**
     * 데일리 루틴의 운동 환경에서 가능한 운동 목록과 루틴 포함 여부를 조회합니다.
     */
    @Operation(
            summary = "데일리 루틴에서 선택 가능한 운동 목록 조회",
            description = "로그인한 회원의 데일리 루틴에 설정된 장소와 보유 기구를 "
                    + "기준으로 가능한 운동을 조회하고, 각 운동의 루틴 포함 여부를 반환합니다."
    )
    @GetMapping("/{dailyRoutineId}/available-exercises")
    public ResponseEntity<DailyRoutineAvailableExerciseResponseDto> getAvailableExercises(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(
                    description = "조회할 데일리 루틴 ID",
                    example = "15",
                    required = true
            )
            @PathVariable("dailyRoutineId") Long dailyRoutineId
    ) {
        return ResponseEntity.ok(
                dailyRoutineExerciseService.getAvailableExercises(
                        memberId,
                        dailyRoutineId
                )
        );
    }

    @Operation(
            summary = "오늘의 루틴 운동 목록 전체 수정",
            description = "오늘의 미시작 루틴의 운동 목록과 처방값을 전체 교체하고, "
                    + "AI가 산출한 주요 운동 부위와 예상 운동 시간을 반영합니다."
    )
    @PutMapping("/{dailyRoutineId}/exercises")
    public ResponseEntity<Void> updateExercises(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(
                    description = "수정할 데일리 루틴 ID",
                    example = "15",
                    required = true
            )
            @PathVariable("dailyRoutineId") Long dailyRoutineId,

            @Valid @RequestBody DailyRoutineExerciseUpdateRequestDto request
    ) {
        dailyRoutineExerciseUpdateService.updateExercises(
                memberId,
                dailyRoutineId,
                request
        );

        return ResponseEntity.noContent().build();
    }
}