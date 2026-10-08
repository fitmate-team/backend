package com.fitmate.backend.member.dto.request;

import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PrimaryLocationUpdateRequestDto {
    @NotNull
    private ExerciseLocation location;
}
