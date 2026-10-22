package com.fitmate.backend.routine.ai.client;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class OpenAiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties properties;

    public OpenAiClient(OpenAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(180));

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .requestFactory(requestFactory)
                .build();
    }

    public <T> T generate(String prompt, Class<T> outputType) {

        Map<String, Object> requestBody = Map.of("model",
                                                 properties.getModel(),
                                                 "input",
                                                 prompt,
                                                 "text",
                                                 Map.of("format", Map.of("type", "json_object")),
                                                 "store",
                                                 false);

        String responseBody;

        try {
            responseBody = restClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

        } catch (RestClientResponseException e) {
            log.warn("OpenAI HTTP 오류: {}", e.getStatusCode().value());
            throw new CustomException(ErrorCode.OPENAI_API_ERROR);

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new CustomException(ErrorCode.OPENAI_TIMEOUT);
            }
            throw new CustomException(ErrorCode.OPENAI_API_ERROR);

        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.OPENAI_API_ERROR);
        }

        if (responseBody == null || responseBody.isBlank()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        try {
            JsonNode response = objectMapper.readTree(responseBody);

            if (response == null || !"completed".equals(response.path("status").asString(null))) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            String outputText = extractOutputText(response);

            return objectMapper.readValue(outputText, outputType);

        } catch (JacksonException e) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }

    private String extractOutputText(JsonNode response) {

        JsonNode outputs = response.path("output");

        if (!outputs.isArray()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        StringBuilder text = new StringBuilder();

        for (JsonNode output : outputs) {

            if (!"message".equals(output.path("type").asString(null))) {
                continue;
            }

            JsonNode contents = output.path("content");

            if (!contents.isArray()) {
                continue;
            }

            for (JsonNode content : contents) {
                String type = content.path("type").asString(null);
                if ("refusal".equals(type)) {
                    throw new CustomException(ErrorCode.OPENAI_RESPONSE_REFUSED);
                }
                if ("output_text".equals(type)) {
                    JsonNode textNode = content.path("text");
                    if (!textNode.isString()) {
                        throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
                    }
                    text.append(textNode.asString());
                }
            }
        }

        if (text.toString().isBlank()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        return text.toString();
    }
}
