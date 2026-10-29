package com.fitmate.backend.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "운동별 조정 추천 선택 결과 요청")
public class ExerciseAdjustmentSelectionsRequestDto {

    @Valid
    @NotNull(message = "운동별 선택 결과는 필수입니다.")
    @Schema(description = "운동별 조정 추천 선택 결과 목록")
    private List<
            @NotNull(message = "운동별 선택 결과는 null일 수 없습니다.")
                    ExerciseSelectionDto
            > exercises;

    @Getter
    @NoArgsConstructor
    @Schema(description = "운동 하나의 조정 추천 선택 결과")
    public static class ExerciseSelectionDto
            extends ExerciseAdjustmentApplyRequestDto {

        @NotNull(message = "운동 ID는 필수입니다.")
        @Schema(description = "운동 ID", example = "2")
        private Long exerciseId;

        @NotNull(message = "조정값 적용 여부는 필수입니다.")
        @Schema(
                description = "조정 추천값 적용 여부. true이면 적용, false이면 그대로 유지",
                example = "true"
        )
        private Boolean apply;
    }
}