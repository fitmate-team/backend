package com.fitmate.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 요청 DTO")
@NoArgsConstructor
@Getter
public class LoginRequestDto {
    @Schema(description = "로그인 아이디 (8~15자)", example = "asdf1234")
    @NotBlank(message = "아이디를 입력해주세요")
    private String loginId;

    @Schema(description = "비밀번호 (영문, 숫자 포함 10자 이상)", example = "asdfasdf1234")
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

}
