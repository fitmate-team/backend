package com.fitmate.backend.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BodyMeasurementCreateRequestDto {

    @Schema(description = "몸무게(kg)", example = "55.4")
    @NotNull(message = "몸무게를 입력해주세요.")
    @Positive(message = "몸무게는 양수여야 합니다.")
    private Double weight;

    @Schema(description = "골격근량(kg), 선택값", example = "23.5")
    @Positive(message = "골격근량은 양수여야 합니다.")
    private Double skeletalMuscleMass;

    @Schema(description = "체지방률(%), 선택값", example = "22.3")
    @Positive(message = "체지방률은 양수여야 합니다.")
    private Double bodyFatPercentage;

    @Schema(description = "체지방량(kg), 선택값", example = "12.4")
    @Positive(message = "체지방량은 양수여야 합니다.")
    private Double bodyFatMass;
}
