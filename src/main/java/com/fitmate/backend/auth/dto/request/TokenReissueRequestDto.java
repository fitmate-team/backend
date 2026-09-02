package com.fitmate.backend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "access 토큰 재발급 요청 DTO")
@Getter
@NoArgsConstructor
public class TokenReissueRequestDto {

    @Schema(description = "리프레시 토큰")
    @NotBlank(message = "리프레시 토큰이 필요합니다")
    private String refreshToken;
}
