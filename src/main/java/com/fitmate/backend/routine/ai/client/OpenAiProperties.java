package com.fitmate.backend.routine.ai.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// yml 에서 값 받아다가 아래 필드로 넣음
@ConfigurationProperties(prefix = "openai")
@Component
@Getter
@Setter
public class OpenAiProperties {

    String apiKey; // OpenAI API 인증키
    String model;
    String baseUrl;

}
