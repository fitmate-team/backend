package com.fitmate.backend.auth.controller;

import com.fitmate.backend.auth.dto.request.LoginRequestDto;
import com.fitmate.backend.auth.dto.request.TokenReissueRequestDto;
import com.fitmate.backend.auth.dto.response.LoginResponseDto;
import com.fitmate.backend.auth.dto.response.TokenReissueResponseDto;
import com.fitmate.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "로그인 API", description = "로그인")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    @SecurityRequirements()
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto responseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Access 토큰 재발급")
    @PostMapping("/reissue")
    @SecurityRequirements()
    public ResponseEntity<TokenReissueResponseDto> reissue(@Valid @RequestBody TokenReissueRequestDto tokenReissueRequestDto) {
        TokenReissueResponseDto responseDto = authService.reissue(tokenReissueRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}
