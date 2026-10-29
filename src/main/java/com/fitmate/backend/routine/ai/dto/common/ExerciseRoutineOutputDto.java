package com.fitmate.backend.routine.ai.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// 재사용
public class ExerciseRoutineOutputDto {

    private String exerciseCode;
    private String nameKo;
    private Integer sets;
    private Integer reps;
    private Integer durationSeconds;
    private Integer weightKg;
    private Integer restSeconds;
}
