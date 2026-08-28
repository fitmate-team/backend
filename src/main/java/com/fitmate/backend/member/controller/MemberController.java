package com.fitmate.backend.member.controller;

import com.fitmate.backend.member.dto.MemberResponseDto;
import com.fitmate.backend.member.dto.SignUpRequestDto;
import com.fitmate.backend.member.dto.SignUpResponseDto;
import com.fitmate.backend.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "회원 API", description = "회원 생성, 정보조회, 수정, 삭제")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "회원 생성")
    @PostMapping
    public ResponseEntity<SignUpResponseDto> createMember(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        SignUpResponseDto responseDto = memberService.createMember(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
