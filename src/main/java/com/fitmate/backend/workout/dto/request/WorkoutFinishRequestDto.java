package com.fitmate.backend.workout.dto.request;

import com.fitmate.backend.workout.domain.PerceivedDifficulty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "운동 종료 요청")
public class WorkoutFinishRequestDto {

    @Schema(description = "오늘의 루틴 ID", example = "28")
    @NotNull(message = "오늘의 루틴 ID는 필수입니다.")
    private Long dailyRoutineId;

    @Schema(description = "종료할 운동 기록 ID", example = "42")
    @NotNull(message = "운동 기록 ID는 필수입니다.")
    private Long workoutRecordId;

    @Schema(
            description = "실제 운동 종료 일시. 종료 버튼을 누른 시각",
            example = "2026-10-16T03:15:00"
    )
    @NotNull(message = "운동 종료 일시는 필수입니다.")
    private LocalDateTime endedAt;

    @Schema(
            description = "휴식 및 일시정지를 제외한 전체 실제 운동 시간(초)",
            example = "1800"
    )
    @NotNull(message = "실제 운동 시간은 필수입니다.")
    @PositiveOrZero(message = "실제 운동 시간은 0 이상이어야 합니다.")
    private Integer actualExerciseSeconds;

    @Schema(description = "전체 실제 휴식 시간(초)", example = "300")
    @NotNull(message = "실제 휴식 시간은 필수입니다.")
    @PositiveOrZero(message = "실제 휴식 시간은 0 이상이어야 합니다.")
    private Integer actualRestSeconds;

    @Schema(description = "전체 운동 중 최소 심박수(bpm). 미측정 시 생략", example = "80")
    private Integer minHeartRate;

    @Schema(description = "전체 운동 중 최대 심박수(bpm). 미측정 시 생략", example = "150")
    private Integer maxHeartRate;

    @Schema(description = "루틴에 포함된 운동별 실제 수행 기록")
    @NotNull(message = "운동 수행 기록 목록은 필수입니다.")
    @Valid
    private List<@NotNull(message = "개별 운동 수행 기록은 null일 수 없습니다.") ExerciseDto> exercises;

    @Schema(
            description = "사용자가 느낀 전체 운동 난이도",
            example = "NORMAL"
    )
    @NotNull(message = "체감 난이도는 필수입니다.")
    private PerceivedDifficulty perceivedDifficulty;

    @Getter
    @NoArgsConstructor
    @Schema(description = "개별 운동 수행 기록")
    public static class ExerciseDto {

        @Schema(description = "루틴에 포함된 운동 ID", example = "105")
        @NotNull(message = "루틴 운동 ID는 필수입니다.")
        private Long routineExerciseId;

        @Schema(
                description = "실제로 완료한 세트 수. 수행하지 않았거나 세트 기반 운동이 아니면 0",
                example = "3"
        )
        @NotNull(message = "완료한 세트 수는 필수입니다.")
        @PositiveOrZero(message = "완료한 세트 수는 0 이상이어야 합니다.")
        private Integer completedSetCount;

        @Schema(
                description = "해당 운동을 실제로 수행한 시간(초). 수행하지 않았다면 0",
                example = "600"
        )
        @NotNull(message = "운동 수행 시간은 필수입니다.")
        @PositiveOrZero(message = "운동 수행 시간은 0 이상이어야 합니다.")
        private Integer completedDurationSeconds;

        @Schema(
                description = "해당 운동의 최소 심박수(bpm). 운동별 난이도 판별에 사용하며 미측정 시 생략",
                example = "80"
        )
        @Positive(message = "최소 심박수는 양수여야 합니다.")
        private Integer minHeartRate;

        @Schema(
                description = "해당 운동의 최대 심박수(bpm). 운동별 난이도 판별에 사용하며 미측정 시 생략",
                example = "150"
        )
        @Positive(message = "최대 심박수는 양수여야 합니다.")
        private Integer maxHeartRate;
    }
}