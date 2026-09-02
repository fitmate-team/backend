package com.fitmate.backend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class TokenReissueResponseDto {
    private String accessToken;

    public static TokenReissueResponseDto from(String accessToken) {
        return new TokenReissueResponseDto(accessToken);
    }
}
