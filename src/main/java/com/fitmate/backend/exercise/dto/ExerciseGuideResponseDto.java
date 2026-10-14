package com.fitmate.backend.exercise.dto;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "운동 가이드 조회 응답")
public class ExerciseGuideResponseDto {

    @Schema(description = "운동 ID", example = "28")
    private Long exerciseId;

    @Schema(description = "운동 이름", example = "덤벨 숄더 프레스")
    private String exerciseName;

    @Schema(description = "운동 가이드 영상 URL")
    private String videoUrl;

    @Schema(description = "운동 가이드 텍스트")
    private String guide;

    @Schema(description = "주요 운동 부위")
    private PrimaryMuscle primaryMuscle;

    public static ExerciseGuideResponseDto from(Exercise exercise) {
        return new ExerciseGuideResponseDto(
                exercise.getId(),
                exercise.getNameKo(),
                exercise.getVideoUrl(),
                exercise.getGuide(),
                exercise.getPrimaryMuscle()
        );
    }
}