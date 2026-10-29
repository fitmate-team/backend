package com.fitmate.backend.routine.ai.dto.custom.prescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CustomAiPrescriptionRequestDto {

    @NotEmpty
    private List<@NotBlank String> exerciseCodes;
}
