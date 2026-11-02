package com.fitmate.backend.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "weeklyWorkout",
        "todayWorkout"
})
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "홈 화면 조회 응답 DTO")
public class HomeResponseDto {

    @Schema(description = "이번 주 운동 현황")
    private WeeklyWorkoutResponseDto weeklyWorkout;

    @Schema(description = "오늘의 운동 정보")
    private TodayWorkoutResponseDto todayWorkout;

    public static HomeResponseDto of(
            WeeklyWorkoutResponseDto weeklyWorkout,
            TodayWorkoutResponseDto todayWorkout
    ) {
        return new HomeResponseDto(
                weeklyWorkout,
                todayWorkout
        );
    }

    @JsonPropertyOrder({
            "hasWeeklyRoutine",
            "completedWorkoutCount",
            "targetWorkoutCount",
            "consecutiveWorkoutDays"
    })
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "이번 주 운동 현황 응답 DTO")
    public static class WeeklyWorkoutResponseDto {

        @Schema(
                description = "이번 주 루틴 존재 여부",
                example = "true"
        )
        private Boolean hasWeeklyRoutine;

        @Schema(
                description = "이번 주 완료한 운동 횟수",
                example = "4"
        )
        private Integer completedWorkoutCount;

        @Schema(
                description = "이번 주 목표 운동 횟수",
                example = "5"
        )
        private Integer targetWorkoutCount;

        @Schema(
                description = "연속 운동 일수",
                example = "12"
        )
        private Integer consecutiveWorkoutDays;

        public static WeeklyWorkoutResponseDto of(
                boolean hasWeeklyRoutine,
                int completedWorkoutCount,
                int targetWorkoutCount,
                int consecutiveWorkoutDays
        ) {
            return new WeeklyWorkoutResponseDto(
                    hasWeeklyRoutine,
                    completedWorkoutCount,
                    targetWorkoutCount,
                    consecutiveWorkoutDays
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "오늘의 운동 응답 DTO")
    public static class TodayWorkoutResponseDto {

        @Schema(
                description = "오늘의 운동 상태. 오늘 운동이 없으면 null입니다.",
                example = "NOT_STARTED",
                nullable = true
        )
        private TodayWorkoutStatus status;

        @Schema(
                description = "오늘 날짜",
                example = "2026-10-30"
        )
        private LocalDate workoutDate;

        @Schema(
                description = "오늘의 일일 루틴 ID. 오늘 운동이 없으면 null입니다.",
                example = "16",
                nullable = true
        )
        private Long dailyRoutineId;

        @Schema(
                description = "오늘의 운동 기록 ID. 기록이 없으면 null입니다.",
                example = "9",
                nullable = true
        )
        private Long workoutRecordId;

        @Schema(
                description = "오늘의 운동 제목. 오늘 운동이 없으면 null입니다.",
                example = "상체",
                nullable = true
        )
        private String title;

        @Schema(
                description = "운동 순서대로 정렬된 운동명 목록. 오늘 운동이 없으면 빈 배열입니다.",
                example = "[\"벤치프레스\", \"숄더프레스\", \"체스트프레스\", \"푸시업\", \"플랭크\"]"
        )
        private List<String> exerciseNames;

        @Schema(
                description = "예상 운동 시간(분). 오늘 운동이 없으면 null입니다.",
                example = "55",
                nullable = true
        )
        private Integer estimatedDurationMinutes;

        @Schema(
                description = "운동 종목 수. 오늘 운동이 없으면 0입니다.",
                example = "5"
        )
        private Integer exerciseCount;

        @Schema(
                description = "평균 휴식 시간(초). 오늘 운동이 없으면 null입니다.",
                example = "60",
                nullable = true
        )
        private Integer averageRestSeconds;

        public static TodayWorkoutResponseDto of(
                TodayWorkoutStatus status,
                LocalDate workoutDate,
                Long dailyRoutineId,
                Long workoutRecordId,
                String title,
                List<String> exerciseNames,
                Integer estimatedDurationMinutes,
                Integer averageRestSeconds
        ) {
            return new TodayWorkoutResponseDto(
                    status,
                    workoutDate,
                    dailyRoutineId,
                    workoutRecordId,
                    title,
                    exerciseNames,
                    estimatedDurationMinutes,
                    exerciseNames.size(),
                    averageRestSeconds
            );
        }

        public static TodayWorkoutResponseDto empty(
                LocalDate workoutDate
        ) {
            return new TodayWorkoutResponseDto(
                    null,
                    workoutDate,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    0,
                    null
            );
        }
    }

    @Schema(
            description = "오늘의 운동 상태",
            enumAsRef = true
    )
    public enum TodayWorkoutStatus {

        @Schema(description = "오늘 운동을 완료하지 않았습니다.")
        NOT_STARTED,

        @Schema(description = "오늘 운동을 완료했습니다.")
        COMPLETED
    }
}