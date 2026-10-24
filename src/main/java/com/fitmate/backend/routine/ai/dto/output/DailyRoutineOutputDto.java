package com.fitmate.backend.routine.ai.dto.output;

import com.fitmate.backend.routine.domain.MainTarget;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyRoutineOutputDto {
    private DayOfWeek day;
    private MainTarget mainTarget;
    private String focus;
    private Integer estimatedMinutes;
    private String dailyReason;
    private List<RoutineExerciseOutputDto> exercises;
}
