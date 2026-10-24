package com.fitmate.backend.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "신체 기록 저장 요청")
public class BodyCompositionCreateRequestDto {

    @Schema(description = "체중(kg)", example = "60.5")
    @NotNull(message = "체중은 필수입니다.")
    @Positive(message = "체중은 0보다 커야 합니다.")
    private Double weight;

    @Schema(description = "골격근량(kg)", example = "25.2")
    @PositiveOrZero(message = "골격근량은 0 이상이어야 합니다.")
    private Double skeletalMuscleMass;

    @Schema(description = "체지방률(%)", example = "23.5")
    @PositiveOrZero(message = "체지방률은 0 이상이어야 합니다.")
    @DecimalMax(value = "100.0", message = "체지방률은 100 이하여야 합니다.")
    private Double bodyFatPercentage;

    @Schema(description = "체지방량(kg)", example = "14.2")
    @PositiveOrZero(message = "체지방량은 0 이상이어야 합니다.")
    private Double bodyFatMass;
}