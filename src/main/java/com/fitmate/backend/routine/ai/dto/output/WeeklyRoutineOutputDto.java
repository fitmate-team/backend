package com.fitmate.backend.routine.ai.dto.output;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WeeklyRoutineOutputDto {
    private RecommendationReasons recommendationReason;
    private List<DailyRoutineOutputDto> weeklyPlan;

    @Getter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RecommendationReasons {
        private RecommendationReason primaryGoalReason;
        private RecommendationReason secondaryGoalReason;
        private RecommendationReason scheduleReason;
    }

    @Getter
    @NoArgsConstructor
    public static class RecommendationReason {
        private String title;
        private String reason;
    }
}
