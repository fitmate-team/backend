package com.fitmate.backend.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원가입 최근 운동 기록")
@Getter
@NoArgsConstructor
public class RecentExerciseRecordRequestDto {

    @Schema(description = "운동 코드", example = "EX_CHEST_BB_BENCH_PRESS")
    @NotBlank(message = "운동 코드를 입력해주세요.")
    private String exerciseCode;

    @Schema(description = "최근 수행 중량(kg)", example = "60")
    @Positive(message = "중량은 양수여야 합니다.")
    private Integer weight;

    @Schema(description = "최근 수행 반복 횟수", example = "8")
    @Positive(message = "반복 횟수는 양수여야 합니다.")
    private Integer reps;

    @Schema(description = "최근 수행 세트 수", example = "4")
    @Positive(message = "세트 수는 양수여야 합니다.")
    private Integer sets;

    @Schema(description = "최근 수행 시간(초)", example = "120")
    @Positive(message = "운동 시간은 양수여야 합니다.")
    private Integer durationSeconds;
}