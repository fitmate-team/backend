package com.fitmate.backend.equipment.controller;

import com.fitmate.backend.equipment.dto.EquipmentResponseDto;
import com.fitmate.backend.equipment.service.EquipmentService;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/equipments")
@Tag(name = "운동 도구 API", description = "운동 기구 조회 API")
public class EquipmentController {
    private final EquipmentService equipmentService;

    @Operation(summary = "운동 장소별 운동 기구 목록 조회")
    @GetMapping
    @SecurityRequirements()
    public ResponseEntity<List<EquipmentResponseDto>> getEquipments(
            @RequestParam ExerciseLocation location) {
        return ResponseEntity.ok(equipmentService.getEquipmentsByLocation(location));
    }
}
