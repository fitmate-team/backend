package com.fitmate.backend.member.controller;

import com.fitmate.backend.member.dto.request.SignUpRequestDto;
import com.fitmate.backend.member.dto.response.LoginIdCheckResponseDto;
import com.fitmate.backend.member.dto.response.MemberResponseDto;
import com.fitmate.backend.member.dto.response.SignUpResponseDto;
import com.fitmate.backend.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "회원 API", description = "회원 생성, 정보조회, 수정, 삭제")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "회원 생성")
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> createMember(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        SignUpResponseDto responseDto = memberService.createMember(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "ID 중복 확인")
    @GetMapping("/id-check")
    public ResponseEntity<LoginIdCheckResponseDto> checkLoginId(@RequestParam String loginId) {
        return ResponseEntity.ok(memberService.checkLoginId(loginId));
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/my-info")
    public ResponseEntity<MemberResponseDto> getMyInfo(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(memberService.getMember(memberId));
    }

}
