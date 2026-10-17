package com.fitmate.backend.workout.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({
        "exerciseId",
        "exerciseName",
        "monthlyRecords"
})
public class ExerciseGrowthResponseDto {

    @Schema(
            description = "선택한 운동의 PK",
            example = "10"
    )
    private final Long exerciseId;

    @Schema(
            description = "선택한 운동명",
            example = "바벨 벤치 프레스"
    )
    private final String exerciseName;

    @Schema(
            description = "이번 달 포함 최근 6개월의 월별 중량 변화. 월 오름차순으로 6개 항목을 반환합니다."
    )
    private final List<WeightMonthlyRecordDto> monthlyRecords;

    public static ExerciseGrowthResponseDto of(
            Long exerciseId,
            String exerciseName,
            List<WeightMonthlyRecordDto> monthlyRecords
    ) {
        return new ExerciseGrowthResponseDto(
                exerciseId,
                exerciseName,
                monthlyRecords
        );
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({
            "year",
            "month",
            "maxWeight",
            "minWeight"
    })
    public static class WeightMonthlyRecordDto {

        @Schema(description = "집계 연도", example = "2026")
        private final int year;

        @Schema(description = "집계 월", example = "10")
        private final int month;

        @Schema(
                description = "1세트 이상 수행한 종료 기록의 최대 설정 중량. kg 단위이며 기록이 없으면 null입니다.",
                example = "55.0"
        )
        private final BigDecimal maxWeight;

        @Schema(
                description = "1세트 이상 수행한 종료 기록의 최소 설정 중량. kg 단위이며 기록이 없으면 null입니다.",
                example = "45.0"
        )
        private final BigDecimal minWeight;

        public static WeightMonthlyRecordDto of(
                int year,
                int month,
                BigDecimal maxWeight,
                BigDecimal minWeight
        ) {
            return new WeightMonthlyRecordDto(
                    year,
                    month,
                    maxWeight,
                    minWeight
            );
        }
    }
}