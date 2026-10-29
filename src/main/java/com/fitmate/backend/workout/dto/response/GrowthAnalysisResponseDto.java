package com.fitmate.backend.workout.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({
        "bodyComposition",
        "workoutGrowth",
        "attendance"
})
public class GrowthAnalysisResponseDto {

    @Schema(description = "최근 신체 측정 정보와 이번 달 포함 최근 6개월의 변화 추이")
    private final BodyCompositionDto bodyComposition;

    @Schema(description = "수행한 중량 운동 목록과 기본 선택 운동의 성장 그래프")
    private final WorkoutGrowthDto workoutGrowth;

    @Schema(description = "이번 달 포함 최근 6개월의 월별 출석률")
    private final AttendanceDto attendance;

    public static GrowthAnalysisResponseDto of(
            BodyCompositionDto bodyComposition,
            WorkoutGrowthDto workoutGrowth,
            AttendanceDto attendance
    ) {
        return new GrowthAnalysisResponseDto(
                bodyComposition,
                workoutGrowth,
                attendance
        );
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"latest", "monthlyRecords"})
    public static class BodyCompositionDto {

        @Schema(description = "지표별 가장 최근의 유효 측정값. 최근 6개월 이전의 측정도 포함합니다.")
        private final LatestBodyCompositionDto latest;

        @Schema(description = "최근 6개월의 월별 최대·최소 측정값. 월 오름차순으로 6개 항목을 반환합니다.")
        private final List<BodyMonthlyRecordDto> monthlyRecords;

        public static BodyCompositionDto of(
                LatestBodyCompositionDto latest,
                List<BodyMonthlyRecordDto> monthlyRecords
        ) {
            return new BodyCompositionDto(latest, monthlyRecords);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({
            "bodyWeight",
            "skeletalMuscleMass",
            "bodyFatPercentage",
            "bodyFatMass"
    })
    public static class LatestBodyCompositionDto {

        @Schema(description = "최근 몸무게. kg 단위")
        private final MeasurementDto bodyWeight;

        @Schema(description = "최근 골격근량. kg 단위")
        private final MeasurementDto skeletalMuscleMass;

        @Schema(description = "최근 체지방률. % 단위")
        private final MeasurementDto bodyFatPercentage;

        @Schema(description = "최근 체지방량. kg 단위")
        private final MeasurementDto bodyFatMass;

        public static LatestBodyCompositionDto of(
                MeasurementDto bodyWeight,
                MeasurementDto skeletalMuscleMass,
                MeasurementDto bodyFatPercentage,
                MeasurementDto bodyFatMass
        ) {
            return new LatestBodyCompositionDto(
                    bodyWeight,
                    skeletalMuscleMass,
                    bodyFatPercentage,
                    bodyFatMass
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"value", "measuredDate"})
    public static class MeasurementDto {

        @Schema(description = "최근 측정값. 측정 이력이 없으면 null입니다.", example = "68.4")
        private final Double value;

        @Schema(description = "측정일. 측정 이력이 없으면 null입니다.", example = "2026-10-17")
        private final LocalDate measuredDate;

        public static MeasurementDto of(
                Double value,
                LocalDate measuredDate
        ) {
            return new MeasurementDto(value, measuredDate);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({
            "year",
            "month",
            "bodyWeight",
            "skeletalMuscleMass",
            "bodyFatPercentage",
            "bodyFatMass"
    })
    public static class BodyMonthlyRecordDto {

        @Schema(description = "집계 연도", example = "2026")
        private final int year;

        @Schema(description = "집계 월", example = "10")
        private final int month;

        @Schema(description = "몸무게의 월별 최대·최소. kg 단위")
        private final RangeDto bodyWeight;

        @Schema(description = "골격근량의 월별 최대·최소. kg 단위")
        private final RangeDto skeletalMuscleMass;

        @Schema(description = "체지방률의 월별 최대·최소. % 단위")
        private final RangeDto bodyFatPercentage;

        @Schema(description = "체지방량의 월별 최대·최소. kg 단위")
        private final RangeDto bodyFatMass;

        public static BodyMonthlyRecordDto of(
                int year,
                int month,
                RangeDto bodyWeight,
                RangeDto skeletalMuscleMass,
                RangeDto bodyFatPercentage,
                RangeDto bodyFatMass
        ) {
            return new BodyMonthlyRecordDto(
                    year,
                    month,
                    bodyWeight,
                    skeletalMuscleMass,
                    bodyFatPercentage,
                    bodyFatMass
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"max", "min"})
    public static class RangeDto {

        @Schema(description = "월 최대 측정값. 해당 지표의 측정 기록이 없으면 null입니다.", example = "69.0")
        private final Double max;

        @Schema(description = "월 최소 측정값. 해당 지표의 측정 기록이 없으면 null입니다.", example = "68.4")
        private final Double min;

        public static RangeDto of(Double max, Double min) {
            return new RangeDto(max, min);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({
            "exercises",
            "selectedExercise"
    })
    public static class WorkoutGrowthDto {

        @Schema(
                description = "최근 6개월에 1세트 이상 수행한 중량 운동 목록. 중량이 null인 기록은 제외하며 운동 ID 오름차순입니다."
        )
        private final List<ExerciseDto> exercises;

        @Schema(
                description = "목록에서 운동 ID가 가장 작은 기본 운동의 그래프. 목록이 비어 있으면 null입니다."
        )
        private final ExerciseGrowthResponseDto selectedExercise;

        public static WorkoutGrowthDto of(
                List<ExerciseDto> exercises,
                ExerciseGrowthResponseDto selectedExercise
        ) {
            return new WorkoutGrowthDto(
                    exercises,
                    selectedExercise
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"exerciseId", "exerciseName"})
    public static class ExerciseDto {

        @Schema(description = "운동 마스터의 PK", example = "10")
        private final Long exerciseId;

        @Schema(description = "운동명", example = "바벨 벤치 프레스")
        private final String exerciseName;

        public static ExerciseDto of(Long exerciseId, String exerciseName) {
            return new ExerciseDto(exerciseId, exerciseName);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"exerciseId", "exerciseName", "monthlyRecords"})
    public static class ExerciseGrowthDto {

        @Schema(description = "선택된 운동의 PK", example = "10")
        private final Long exerciseId;

        @Schema(description = "선택된 운동명", example = "바벨 벤치 프레스")
        private final String exerciseName;

        @Schema(description = "최근 6개월의 중량 변화. 월 오름차순으로 6개 항목을 반환합니다.")
        private final List<WeightMonthlyRecordDto> monthlyRecords;

        public static ExerciseGrowthDto of(
                Long exerciseId,
                String exerciseName,
                List<WeightMonthlyRecordDto> monthlyRecords
        ) {
            return new ExerciseGrowthDto(
                    exerciseId,
                    exerciseName,
                    monthlyRecords
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"year", "month", "maxWeight", "minWeight"})
    public static class WeightMonthlyRecordDto {

        @Schema(description = "집계 연도", example = "2026")
        private final int year;

        @Schema(description = "집계 월", example = "10")
        private final int month;

        @Schema(description = "1세트 이상 수행한 종료 기록의 최대 설정 중량. kg 단위이며 기록이 없으면 null입니다.", example = "55.0")
        private final Integer maxWeight;

        @Schema(description = "1세트 이상 수행한 종료 기록의 최소 설정 중량. kg 단위이며 기록이 없으면 null입니다.", example = "45.0")
        private final Integer minWeight;

        public static WeightMonthlyRecordDto of(
                int year,
                int month,
                Integer maxWeight,
                Integer minWeight
        ) {
            return new WeightMonthlyRecordDto(
                    year,
                    month,
                    maxWeight,
                    minWeight
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"monthlyRecords"})
    public static class AttendanceDto {

        @Schema(description = "최근 6개월의 월별 출석률. 월 오름차순으로 6개 항목을 반환합니다.")
        private final List<AttendanceMonthlyRecordDto> monthlyRecords;

        public static AttendanceDto of(
                List<AttendanceMonthlyRecordDto> monthlyRecords
        ) {
            return new AttendanceDto(monthlyRecords);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonPropertyOrder({"year", "month", "attendanceRate"})
    public static class AttendanceMonthlyRecordDto {

        @Schema(description = "집계 연도", example = "2026")
        private final int year;

        @Schema(description = "집계 월", example = "10")
        private final int month;

        @Schema(
                description = "오늘까지 예정된 일일 루틴 중 시작한 루틴의 비율. 종료 여부와 관계없이 출석이며 정수 반올림합니다. 예정 루틴이 없으면 null입니다.",
                example = "70"
        )
        private final Integer attendanceRate;

        public static AttendanceMonthlyRecordDto of(
                int year,
                int month,
                Integer attendanceRate
        ) {
            return new AttendanceMonthlyRecordDto(
                    year,
                    month,
                    attendanceRate
            );
        }
    }
}