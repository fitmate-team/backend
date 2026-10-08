package com.fitmate.backend.member.dto.request;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
public class GymRequestDto {

    @NotBlank
    private String gymName;

    @NotNull
    private Set<String> equipmentCodes = new HashSet<>();

    public WorkoutEnvironment toWorkoutEnvironment(Member member, Set<Equipment> equipment) {
        return WorkoutEnvironment.builder()
                .member(member)
                .gymName(this.gymName)
                .locationType(ExerciseLocation.GYM)
                .equipment(equipment)
                .build();
    }
}