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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-routines")
@Tag(
        name = "데일리 루틴 운동 API",
        description = "데일리 루틴 직접 수정을 위한 운동 조회 API"
)
public class DailyRoutineExerciseController {

    private final DailyRoutineExerciseService dailyRoutineExerciseService;

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
}