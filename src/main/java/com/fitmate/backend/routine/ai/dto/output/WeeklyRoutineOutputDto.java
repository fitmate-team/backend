package com.fitmate.backend.routine.ai.dto.output;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WeeklyRoutineOutputDto {
    private RecommendationReasons recommendationReason;
    private List<WeeklyDayOutputDto> weeklyPlan;

    @Getter
    @NoArgsConstructor
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
