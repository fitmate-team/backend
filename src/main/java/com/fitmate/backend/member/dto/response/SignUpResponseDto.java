package com.fitmate.backend.member.dto.response;

import com.fitmate.backend.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResponseDto {
    private Long id;
    private String loginId;

    public static SignUpResponseDto from(Member member) {
        return new SignUpResponseDto(
                member.getId(),
                member.getLoginId()
        );
     }
}
