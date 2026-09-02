package com.fitmate.backend.member.dto.request;

import com.fitmate.backend.member.domain.ExerciseGoal;
import com.fitmate.backend.member.domain.ExerciseLevel;
import com.fitmate.backend.member.domain.Gender;
import com.fitmate.backend.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequestDto {

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

}
