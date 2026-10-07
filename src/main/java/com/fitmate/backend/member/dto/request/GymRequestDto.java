package com.fitmate.backend.member.dto.request;

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
}