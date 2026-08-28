package com.fitmate.backend.auth.dto;

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

    public static LoginResponseDto of(Member member, String accessToken) {
        return new LoginResponseDto(member.getId(),
                                    member.getLoginId(),
                                    member.getNickname(),
                                    accessToken);
    }
}
