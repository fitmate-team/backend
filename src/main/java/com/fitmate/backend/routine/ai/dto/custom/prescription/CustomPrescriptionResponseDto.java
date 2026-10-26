package com.fitmate.backend.routine.ai.dto.custom.prescription;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomPrescriptionResponseDto {

    private List<ExerciseRecommendationDto> exercises;

    public static CustomPrescriptionResponseDto of(List<ExerciseRecommendationDto> exercises) {
        return new CustomPrescriptionResponseDto(exercises);
    }

    @Getter
    @AllArgsConstructor
    public static class ExerciseRecommendationDto {
        private String exerciseCode;
        private RecommendationDto recommendation;

        public static ExerciseRecommendationDto of(String exerciseCode,
                                                   RecommendationDto recommendation) {
            return new ExerciseRecommendationDto(exerciseCode, recommendation);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class RecommendationDto {
        private BigDecimal weightKg;
        private Integer reps;
        private Integer sets;
        private Integer durationSeconds;
        private Integer restSeconds;

        public static CustomPrescriptionResponseDto.RecommendationDto from(
                CustomPrescriptionOutputDto.PrescriptionDto prescription) {
            return new CustomPrescriptionResponseDto.RecommendationDto(prescription.getWeightKg(),
                                                                       prescription.getReps(),
                                                                       prescription.getSets(),
                                                                       prescription.getDurationSeconds(),
                                                                       prescription.getRestSeconds());
        }

    }
}