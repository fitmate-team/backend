package com.fitmate.backend.member.controller;

import com.fitmate.backend.member.dto.request.BodyCompositionCreateRequestDto;
import com.fitmate.backend.member.service.BodyCompositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/me/body-compositions")
@Tag(name = "신체 기록 API", description = "회원의 신체 기록 저장 및 조회")
public class BodyCompositionController {

    private final BodyCompositionService bodyCompositionService;

    @Operation(
            summary = "신체 기록 저장",
            description = "로그인한 회원의 체중, 골격근량, 체지방률, 체지방량을 새로운 기록으로 저장합니다."
    )
    @PostMapping
    public ResponseEntity<Void> createBodyComposition(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody BodyCompositionCreateRequestDto requestDto
    ) {
        bodyCompositionService.createBodyComposition(memberId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}