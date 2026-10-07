package com.fitmate.backend.member.dto.response;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class WorkoutEnvironmentResponseDto {

    private Long id;
    private String gymName;
    private ExerciseLocation locationType;
    private Set<String> equipmentCodes;

    public static WorkoutEnvironmentResponseDto from(WorkoutEnvironment environment) {
        return new WorkoutEnvironmentResponseDto(
                environment.getId(),
                environment.getGymName(),
                environment.getLocationType(),
                environment.getEquipment()
                        .stream()
                        .map(Equipment::getEquipmentCode)
                        .collect(Collectors.toSet())
        );
    }
}