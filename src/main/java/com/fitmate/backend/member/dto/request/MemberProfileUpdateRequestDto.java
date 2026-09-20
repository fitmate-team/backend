package com.fitmate.backend.member.dto.request;

import com.fitmate.backend.member.domain.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
public class MemberProfileUpdateRequestDto {

    // MemberProfile
    @Schema(description = "성별", example = "FEMALE")
    @NotNull(message = "성별을 선택해주세요.")
    private Gender gender;

    @Schema(description = "나이", example = "25")
    @NotNull(message = "나이를 입력해주세요.")
    @Positive(message = "나이는 양수여야 합니다.")
    private Integer age;

    @Schema(description = "키(cm)", example = "165.5")
    @NotNull(message = "키를 입력해주세요.")
    @Positive(message = "키는 양수여야 합니다.")
    private Double height;

    @Schema(description = "운동 경험 수준", example = "BEGINNER")
    @NotNull(message = "운동 경험 수준을 선택해주세요.")
    private ExerciseLevel exerciseLevel;

    @Schema(description = "현재 운동 상태", example = "REGULARLY_EXERCISING")
    @NotNull(message = "현재 운동 상태를 선택해주세요.")
    private CurrentExerciseStatus currentExerciseStatus;

    @Schema(description = "주 운동 목표", example = "MUSCLE_GAIN")
    @NotNull(message = "운동 목표를 선택해주세요.")
    private PrimaryGoal primaryGoal;

    @Schema(
            description = "운동 목표 세부 전략",
            example = "MUSCLE_GAIN_HYPERTROPHY_FOCUS"
    )
    @NotNull(message = "운동 목표 전략을 선택해주세요.")
    private GoalStrategy goalStrategy;

    @Schema(description = "주간 운동 횟수", example = "3")
    @NotNull(message = "주간 운동 횟수를 선택해주세요.")
    @Min(value = 1, message = "주간 운동 횟수는 최소 1회여야 합니다.")
    @Max(value = 7, message = "주간 운동 횟수는 최대 7회입니다.")
    private Integer weeklyFrequency;

    @Schema(description = "1회 운동 가능 시간(분)", example = "60")
    @NotNull(message = "운동 시간을 선택해주세요.")
    @Positive(message = "운동 시간은 양수여야 합니다.")
    private Integer sessionMinutes;

    @Schema(description = "운동 장소", example = "GYM")
    @NotNull(message = "운동 장소를 선택해주세요.")
    private ExerciseLocation exerciseLocation;

    @Schema(
            description = "운동 가능한 요일",
            example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]"
    )
    @NotEmpty(message = "운동 가능한 요일을 하나 이상 선택해주세요.")
    private Set<DayOfWeek> availableDays;

    @Schema(
            description = "운동 시 피하고 싶은 신체 부위",
            example = "[\"KNEE\", \"LOWER_BACK\"]"
    )
    private Set<BodyArea> avoidBodyAreas = new HashSet<>();

}
