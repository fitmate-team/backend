package com.fitmate.backend.exercise.controller;

import com.fitmate.backend.exercise.dto.ExerciseResponseDto;
import com.fitmate.backend.exercise.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercises")
@Tag(name = "운동 API", description = "운동 조회 API")
public class ExerciseController {
    private final ExerciseService exerciseService;

    @Operation(summary = "운동 목록 조회")
    @GetMapping
    public ResponseEntity<List<ExerciseResponseDto>> getExercises() {
        return ResponseEntity.ok(exerciseService.getExercises());
    }
}
