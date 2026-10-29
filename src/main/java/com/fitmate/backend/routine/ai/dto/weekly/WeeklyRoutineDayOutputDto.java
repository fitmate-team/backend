package com.fitmate.backend.routine.ai.dto.weekly;

import com.fitmate.backend.routine.ai.dto.common.ExerciseRoutineOutputDto;
import com.fitmate.backend.routine.domain.MainTarget;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@NoArgsConstructor
public class WeeklyRoutineDayOutputDto {
    private DayOfWeek day;
    private MainTarget mainTarget;
    private String focus;
    private Integer estimatedMinutes;
    private String dailyReason;
    private List<ExerciseRoutineOutputDto> exercises;


    public void updateEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

}
