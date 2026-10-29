package com.fitmate.backend.routine.ai.dto.custom.summary;

import com.fitmate.backend.routine.domain.MainTarget;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomSummaryOutputDto {

    private MainTarget mainTarget;
    private Integer estimatedMinutes;
}
