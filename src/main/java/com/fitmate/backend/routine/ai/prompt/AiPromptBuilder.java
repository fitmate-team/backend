package com.fitmate.backend.routine.ai.prompt;

import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomExerciseInputDto;
import com.fitmate.backend.routine.ai.dto.custom.prescription.CustomPrescriptionInputDto;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomSummaryInputDto;
import com.fitmate.backend.routine.ai.dto.daily.DailyRoutineInputDto;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.fitmate.backend.routine.ai.dto.weekly.WeeklyRoutineInputDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AiPromptBuilder {

    private static final String PROMPT_PATH1 = "prompts/fitmate-production.txt";
    private static final String PROMPT_PATH2 = "prompts/fitmate-daily-replacement.txt";
    private static final String PROMPT_PATH3 = "prompts/fitmate-custom-exercises.txt";
    private static final String PROMPT_PATH4 = "prompts/fitmate-custom-prescription.txt";
    private static final String PROMPT_PATH5 = "fitmate-custom-summary.txt";

    private final ObjectMapper objectMapper;
    private final String weeklyPromptTemplate;
    private final String dailyPromptTemplate;
    private final String customExercisePromptTemplate;
    private final String customPrescriptionPromptTemplate;
    private final String customSummaryPromptTemplate;

    public AiPromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.weeklyPromptTemplate = loadPromptTemplate(PROMPT_PATH1);
        this.dailyPromptTemplate = loadPromptTemplate(PROMPT_PATH2);
        this.customExercisePromptTemplate = loadPromptTemplate(PROMPT_PATH3);
        this.customPrescriptionPromptTemplate = loadPromptTemplate(PROMPT_PATH4);
        this.customSummaryPromptTemplate = loadPromptTemplate(PROMPT_PATH5);
    }

    public String build(WeeklyRoutineInputDto request) {
        try {

            String userProfileJson = objectMapper.writeValueAsString(request.getUserProfile());
            String eligibleExercisesJson =
                    objectMapper.writeValueAsString(request.getEligibleExercises());
            String recentExerciseRecordsJson =
                    objectMapper.writeValueAsString(request.getRecentExerciseRecords());

            return weeklyPromptTemplate.replace("{{USER_PROFILE}}", userProfileJson)
                    .replace("{{ELIGIBLE_EXERCISES}}", eligibleExercisesJson)
                    .replace("{{RECENT_EXERCISE_RECORDS}}", recentExerciseRecordsJson);

        } catch (JacksonException e) {
            throw new IllegalStateException("AI 요청 데이터를 JSON으로 변환하지 못했습니다.", e);
        }
    }

    public String build(DailyRoutineInputDto request) {
        try {

            String userProfileJson = objectMapper.writeValueAsString(request.getUserProfile());
            String eligibleExercisesJson =
                    objectMapper.writeValueAsString(request.getEligibleExercises());
            String recentExerciseRecordsJson =
                    objectMapper.writeValueAsString(request.getRecentExerciseRecords());
            String originalExerciseRecordsJson =
                    objectMapper.writeValueAsString(request.getOriginalExercises());

            return dailyPromptTemplate.replace("{{USER_PROFILE}}", userProfileJson)
                    .replace("{{ELIGIBLE_EXERCISES}}", eligibleExercisesJson)
                    .replace("{{RECENT_EXERCISE_RECORDS}}", recentExerciseRecordsJson)
                    .replace("{{ORIGINAL_EXERCISES}}", originalExerciseRecordsJson);

        } catch (JacksonException e) {
            throw new IllegalStateException("AI 요청 데이터를 JSON으로 변환하지 못했습니다.", e);
        }
    }

    public String build(CustomExerciseInputDto request) {
        try {

            String userProfileJson = objectMapper.writeValueAsString(request.getUserProfile());
            String eligibleExercisesJson =
                    objectMapper.writeValueAsString(request.getEligibleExercises());

            return customExercisePromptTemplate.replace("{{USER_PROFILE}}", userProfileJson)
                    .replace("{{ELIGIBLE_EXERCISES}}", eligibleExercisesJson);

        } catch (JacksonException e) {
            throw new IllegalStateException("AI 요청 데이터를 JSON으로 변환하지 못했습니다.", e);
        }
    }

    public String build(CustomPrescriptionInputDto request) {
        try {

            String userProfileJson = objectMapper.writeValueAsString(request.getUserProfile());
            String exercisesJson = objectMapper.writeValueAsString(request.getExercises());

            return customPrescriptionPromptTemplate.replace("{{USER_PROFILE}}", userProfileJson)
                    .replace("{{EXERCISES}}", exercisesJson);

        } catch (JacksonException e) {
            throw new IllegalStateException("AI 요청 데이터를 JSON으로 변환하지 못했습니다.", e);
        }
    }

    public String build(CustomSummaryInputDto request) {
        try {

            String exercisesJson = objectMapper.writeValueAsString(request.getExercises());

            return customSummaryPromptTemplate.replace("{{EXERCISES}}", exercisesJson);

        } catch (JacksonException e) {
            throw new IllegalStateException("AI 요청 데이터를 JSON으로 변환하지 못했습니다.", e);
        }
    }


    private String loadPromptTemplate(String path) {
        ClassPathResource resource = new ClassPathResource(path);

        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("AI 프롬프트 파일을 불러오지 못했습니다.", e);
        }
    }
}