package com.fitmate.backend.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
public class EquipmentUpdateRequestDto {

    @NotNull
    private Set<String> equipmentCodes = new HashSet<>();
}