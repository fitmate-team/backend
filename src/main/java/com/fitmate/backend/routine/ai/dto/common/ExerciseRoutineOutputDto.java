
package com.fitmate.backend.routine.ai.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExerciseRoutineOutputDto {

    private String exerciseCode;
    private String nameKo;
    private Integer sets;
    private Integer reps;
    private Integer durationSeconds;
    private Integer weightKg;
    private Integer restSeconds;

    // ⑤ 조정값이 실제 적용된 경우에만 응답에 포함
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long appliedAdjustmentId;

    // 기존 7개 인자 생성자 유지
    public ExerciseRoutineOutputDto(
            String exerciseCode,
            String nameKo,
            Integer sets,
            Integer reps,
            Integer durationSeconds,
            Integer weightKg,
            Integer restSeconds
    ) {
        this.exerciseCode = exerciseCode;
        this.nameKo = nameKo;
        this.sets = sets;
        this.reps = reps;
        this.durationSeconds = durationSeconds;
        this.weightKg = weightKg;
        this.restSeconds = restSeconds;
    }

    // AI가 만든 처방을 ⑤ 대기 조정값으로 덮어쓰기
    public void applyAdjustment(
            Long adjustmentId,
            Integer weightKg,
            Integer reps,
            Integer sets,
            Integer durationSeconds
    ) {
        this.appliedAdjustmentId = adjustmentId;
        this.weightKg = weightKg;
        this.reps = reps;
        this.sets = sets;
        this.durationSeconds = durationSeconds;

        // restSeconds는 변경하지 않음
    }
}
