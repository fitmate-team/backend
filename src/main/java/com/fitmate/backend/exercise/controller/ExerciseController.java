package com.fitmate.backend.exercise.controller;

import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import com.fitmate.backend.exercise.dto.ExerciseGuideResponseDto;
import com.fitmate.backend.exercise.dto.ExerciseResponseDto;
import com.fitmate.backend.exercise.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercises")
@Tag(name = "운동 API", description = "운동 조회 API")
public class ExerciseController {
    private final ExerciseService exerciseService;

    @Operation(summary = "운동 목록 조회")
    @GetMapping
    @SecurityRequirements()
    public ResponseEntity<List<ExerciseResponseDto>> getExercises(@RequestParam(required = false) String keyword,
                                                                  @RequestParam(required = false) PrimaryMuscle muscle) {
        return ResponseEntity.ok(exerciseService.getExercises(keyword, muscle));
    }

    @Operation(summary = "운동 가이드 다건 조회", description = "운동 ID 목록을 전달받아 각 운동의 이름, 가이드 영상, 설명 및 주요 " +
            "운동 부위를 조회합니다.")
    @GetMapping("/guides")
    public ResponseEntity<List<ExerciseGuideResponseDto>> getExerciseGuides(@Parameter(description = "조회할 운동 ID 목록", example = "28,67,69") @RequestParam List<Long> exerciseIds) {
        return ResponseEntity.ok(exerciseService.getExerciseGuides(exerciseIds));
    }

    @Operation(summary = "선택한 운동 환경에서 가능한 운동 목록 조회")
    @GetMapping("/available")
    public ResponseEntity<List<ExerciseResponseDto>> getAvailableExercises(@AuthenticationPrincipal Long memberId,
                                                                           @RequestParam Long environmentId,
                                                                           @RequestParam(required = false) String keyword,
                                                                           @RequestParam(required = false) PrimaryMuscle muscle) {
        return ResponseEntity.ok(exerciseService.getAvailableExercises(memberId,
                                                                       environmentId,
                                                                       keyword,
                                                                       muscle));
    }
}
