package com.fitmate.backend.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
public class WorkoutEnvironmentUpdateRequestDto {
    private String gymName;
    private Set<String> equipmentCodes;
}