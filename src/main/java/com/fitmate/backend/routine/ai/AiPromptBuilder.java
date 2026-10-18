package com.fitmate.backend.routine.ai;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.fitmate.backend.routine.dto.ai.AiRoutineRequestDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AiPromptBuilder {

    private static final String PROMPT_PATH = "prompts/fitmate-production-v0.8.0.txt";

    private final ObjectMapper objectMapper;
    private final String promptTemplate;

    public AiPromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.promptTemplate = loadPromptTemplate();
    }

    public String build(AiRoutineRequestDto request) {
        try {
            String userProfileJson = objectMapper.writeValueAsString(request.getUserProfile());

            String eligibleExercisesJson =
                    objectMapper.writeValueAsString(request.getEligibleExercises());

            String recentExerciseRecordsJson =
                    objectMapper.writeValueAsString(request.getRecentExerciseRecords());

            return promptTemplate.replace("{{USER_PROFILE}}", userProfileJson)
                    .replace("{{ELIGIBLE_EXERCISES}}", eligibleExercisesJson)
                    .replace("{{RECENT_EXERCISE_RECORDS}}", recentExerciseRecordsJson);

        } catch (JacksonException e) {
            throw new IllegalStateException("AI 요청 데이터를 JSON으로 변환하지 못했습니다.", e);
        }
    }

    private String loadPromptTemplate() {
        ClassPathResource resource = new ClassPathResource(PROMPT_PATH);

        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("AI 프롬프트 파일을 불러오지 못했습니다.", e);
        }
    }
}