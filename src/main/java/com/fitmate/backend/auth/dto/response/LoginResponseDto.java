package com.fitmate.backend.auth.dto.response;

import com.fitmate.backend.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String loginId;
    private String nickname;
    private String accessToken;
    private String refreshToken;

    public static LoginResponseDto of(Member member, String accessToken, String refreshToken) {
        return new LoginResponseDto(member.getId(),
                                    member.getLoginId(),
                                    member.getNickname(),
                                    accessToken, refreshToken);
    }
}
