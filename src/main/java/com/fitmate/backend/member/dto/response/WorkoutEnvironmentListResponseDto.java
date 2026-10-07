package com.fitmate.backend.member.dto.response;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class WorkoutEnvironmentListResponseDto {

    private ExerciseLocation primaryLocation;
    private List<WorkoutEnvironmentResponseDto> environments;

    public static WorkoutEnvironmentListResponseDto of(
            ExerciseLocation primaryLocation,
            List<WorkoutEnvironmentResponseDto> environments
    ) {
        return new WorkoutEnvironmentListResponseDto(
                primaryLocation,
                environments
        );
    }

}