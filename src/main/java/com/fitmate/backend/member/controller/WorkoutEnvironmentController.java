package com.fitmate.backend.member.controller;

import com.fitmate.backend.member.dto.response.WorkoutEnvironmentListResponseDto;
import com.fitmate.backend.member.service.WorkoutEnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/workout-environments")
public class WorkoutEnvironmentController {
    private final WorkoutEnvironmentService workoutEnvironmentService;

    @GetMapping
    public ResponseEntity<WorkoutEnvironmentListResponseDto> getWorkoutEnvironments(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(workoutEnvironmentService.);

    }

    @PatchMapping("/primary-location")
    @PutMapping("/{locationType}")
    @PostMapping("/gyms")
    @PutMapping("/gyms/{environmentId}")

}
