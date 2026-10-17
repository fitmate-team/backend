package com.fitmate.backend.workout.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@JsonPropertyOrder({
        "year",
        "month",
        "workoutCount",
        "totalExerciseSeconds",
        "workoutDates",
        "records"
})
public class MonthlyWorkoutRecordResponseDto {

    @Schema(description = "조회 연도", example = "2026")
    private int year;

    @Schema(description = "조회 월", example = "9")
    private int month;

    @Schema(
            description = "해당 월에 종료된 운동 횟수. 중도 종료도 포함합니다.",
            example = "8"
    )
    private int workoutCount;

    @Schema(
            description = "해당 월의 실제 운동 시간 합계. 초 단위이며 휴식 시간은 제외합니다.",
            example = "28800"
    )
    private long totalExerciseSeconds;

    @Schema(
            description = "달력에 표시할 운동 시작 날짜 목록. 중복 없이 날짜 오름차순으로 반환합니다.",
            example = "[\"2026-09-01\", \"2026-09-03\", \"2026-09-08\"]"
    )
    private List<LocalDate> workoutDates;

    @Schema(
            description = "월별 운동 기록 목록. 운동 시작 일시 내림차순으로 반환합니다.",
            example = "[]"
    )
    private List<RecordDto> records;

    public static MonthlyWorkoutRecordResponseDto of(
            int year,
            int month,
            List<RecordDto> records
    ) {
        MonthlyWorkoutRecordResponseDto response =
                new MonthlyWorkoutRecordResponseDto();

        response.year = year;
        response.month = month;
        response.workoutCount = records.size();

        response.totalExerciseSeconds = records.stream()
                .mapToLong(RecordDto::getActualExerciseSeconds)
                .sum();

        response.workoutDates = records.stream()
                .map(RecordDto::getDate)
                .distinct()
                .sorted()
                .toList();

        response.records = records;

        return response;
    }

    @Getter
    @JsonPropertyOrder({
            "workoutRecordId",
            "date",
            "mainTarget",
            "actualExerciseSeconds",
            "completionRate",
            "maxHeartRate",
            "exerciseNames"
    })
    public static class RecordDto {

        @Schema(
                description = "운동 기록 ID. 선택한 기록의 상세 조회 시 경로 변수로 전달합니다.",
                example = "31"
        )
        private Long workoutRecordId;

        @Schema(
                description = "한국 시간 기준 운동 시작 날짜. 일별 상세 조회 API의 date 파라미터로 전달합니다.",
                example = "2026-09-24"
        )
        private LocalDate date;

        @Schema(
                description = "운동 부위 표시명",
                example = "상체"
        )
        private String mainTarget;

        @Schema(
                description = "실제 운동 시간. 초 단위이며 휴식 시간은 제외합니다.",
                example = "3120"
        )
        private int actualExerciseSeconds;

        @Schema(
                description = "미수행 운동을 포함한 운동별 수행률의 평균. 최종 평균을 반올림한 0~100의 정수입니다.",
                example = "92"
        )
        private int completionRate;

        @Schema(
                description = "최대 심박수. 단위는 bpm이며 미측정 시 null입니다.",
                example = "156"
        )
        private Integer maxHeartRate;

        @Schema(
                description = "실제로 수행한 운동명 목록. 세트 기반은 1세트 이상, 시간 기반은 0초 초과인 운동만 루틴 순서대로 반환합니다.",
                example = "[\"벤치프레스\", \"인클라인 덤벨프레스\"]"
        )
        private List<String> exerciseNames;

        public static RecordDto of(
                Long workoutRecordId,
                LocalDate date,
                String mainTarget,
                int actualExerciseSeconds,
                int completionRate,
                Integer maxHeartRate,
                List<String> exerciseNames
        ) {
            RecordDto response = new RecordDto();

            response.workoutRecordId = workoutRecordId;
            response.date = date;
            response.mainTarget = mainTarget;
            response.actualExerciseSeconds = actualExerciseSeconds;
            response.completionRate = completionRate;
            response.maxHeartRate = maxHeartRate;
            response.exerciseNames = exerciseNames;

            return response;
        }
    }
}