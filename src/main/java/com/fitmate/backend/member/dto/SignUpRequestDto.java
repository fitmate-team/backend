package com.fitmate.backend.member.dto;

import com.fitmate.backend.member.domain.ExerciseGoal;
import com.fitmate.backend.member.domain.ExerciseLevel;
import com.fitmate.backend.member.domain.Gender;
import com.fitmate.backend.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원가입 요청 DTO")
@NoArgsConstructor // 기본 생성자
@Getter
public class SignUpRequestDto {

    @Schema(description = "로그인 아이디 (8~15자)", example = "asdf1234")
    @NotBlank(message = "아이디를 입력해주세요")
    @Size(min = 8, max = 15)
    private String loginId;

    @Schema(description = "비밀번호 (영문, 숫자 포함 10자 이상)", example = "asdfasdf1234")
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{10,}$",
        message = "비밀번호는 영문과 숫자를 포함한 10자리 이상이어야 합니다."
    )
    private String password;

    @Schema(description = "사용자 닉네임 (2~10자)", example = "체리")
    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 2, max = 10)
    private String nickname;

    @Schema(description = "성별 (예: MALE, FEMALE)", example = "FEMALE")
    @NotNull
    private Gender gender;

    @Schema(description = "키", example = "165.5")
    @NotNull
    @Positive
    private Double height;

    @Schema(description = "몸무게", example = "55.4")
    @NotNull
    @Positive
    private Double weight;

    @Schema(description = "운동 수준 (예: HIGH, MEDIUM, LOW", example = "LOW")
    @NotNull
    private ExerciseLevel exerciseLevel;

    @Schema(description = "운동 목표 (예: DIET, MUSCLE_GAIN, FITNESS)", example = "DIET")
    @NotNull
    private ExerciseGoal exerciseGoal;

    @Schema(description = "목표 몸무게 (선택값)", example = "50.0")
    @Positive
    private Double targetWeight;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .loginId(this.loginId)
                .password(encodedPassword)
                .nickname(this.nickname)
                .gender(this.gender)
                .height(this.height)
                .weight(this.weight)
                .exerciseLevel(this.exerciseLevel)
                .exerciseGoal(this.exerciseGoal)
                .targetWeight(this.targetWeight)
                .build();
    }
}
