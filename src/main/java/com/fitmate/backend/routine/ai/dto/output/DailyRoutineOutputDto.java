package com.fitmate.backend.routine.ai.dto.output;

import com.fitmate.backend.routine.domain.MainTarget;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@NoArgsConstructor
public class DailyRoutineOutputDto {
    private DayOfWeek day;
    private MainTarget mainTarget;
    private String focus;
    private Integer estimatedMinutes;
    private String dailyReason;
    private List<RoutineExerciseOutputDto> exercises;
}
