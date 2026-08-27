package com.fitmate.backend.member.dto;

import com.fitmate.backend.member.domain.ExerciseGoal;
import com.fitmate.backend.member.domain.ExerciseLevel;
import com.fitmate.backend.member.domain.Gender;
import com.fitmate.backend.member.domain.Member;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor // 기본 생성자
@Getter
public class SignUpRequestDto {

    @NotBlank(message = "아이디를 입력해주세요")
    @Size(min = 8, max = 15)
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{10,}$",
        message = "비밀번호는 영문과 숫자를 포함한 10자리 이상이어야 합니다."
    )
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 2, max = 10)
    private String nickname;

    @NotNull
    private Gender gender;

    @NotNull
    @Positive
    private Double height;

    @NotNull
    @Positive
    private Double weight;

    @NotNull
    private ExerciseLevel exerciseLevel;

    @NotNull
    private ExerciseGoal exerciseGoal;

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
