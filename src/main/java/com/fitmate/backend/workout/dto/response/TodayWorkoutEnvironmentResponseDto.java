package com.fitmate.backend.workout.dto.response;

import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TodayWorkoutEnvironmentResponseDto {

    private Long scheduledEnvironmentId;
    private List<EnvironmentDto> environments;

    @Getter
    @AllArgsConstructor
    public static class EnvironmentDto {

        private Long id;
        private String gymName;
        private ExerciseLocation locationType;

        public static EnvironmentDto from(
                WorkoutEnvironment environment
        ) {
            return new EnvironmentDto(
                    environment.getId(),
                    environment.getGymName(),
                    environment.getLocationType()
            );
        }
    }

    public static TodayWorkoutEnvironmentResponseDto of(
            Long selectedEnvironmentId,
            List<EnvironmentDto> environments
    ) {
        return new TodayWorkoutEnvironmentResponseDto(
                selectedEnvironmentId,
                environments
        );
    }
}