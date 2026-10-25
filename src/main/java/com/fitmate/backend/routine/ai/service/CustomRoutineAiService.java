package com.fitmate.backend.routine.ai.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.ai.client.OpenAiClient;
import com.fitmate.backend.routine.ai.dto.common.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.AiCustomExerciseUserProfileDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomAiExerciseRequestDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomExerciseInputDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomExerciseOutputDto;
import com.fitmate.backend.routine.ai.prompt.AiPromptBuilder;
import com.fitmate.backend.routine.ai.validator.CustomExerciseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomRoutineAiService {
    private final MemberProfileRepository memberProfileRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final HardFilterService hardFilterService;
    private final AiPromptBuilder aiPromptBuilder;
    private final OpenAiClient openAiClient;
    private final CustomExerciseValidator validator;

    public String createAiPrompt(Long memberId, CustomAiExerciseRequestDto requestDto) {
        // 프롬포트 완성
        CustomExerciseInputDto input = buildGenerationInput(memberId, requestDto);
        return aiPromptBuilder.build(input);
    }

    public CustomExerciseOutputDto generateExerciseRecommendations(Long memberId,
                                                                   CustomAiExerciseRequestDto requestDto) {
        CustomExerciseInputDto input = buildGenerationInput(memberId, requestDto);
        String prompt = aiPromptBuilder.build(input);
        CustomExerciseOutputDto output = openAiClient.generate(prompt,
                                                               CustomExerciseOutputDto.class);
        validator.validate(input, output);
        return output;
    }

    private CustomExerciseInputDto buildGenerationInput(Long memberId,
                                                        CustomAiExerciseRequestDto requestDto) {
        // DB 조회
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_PROFILE_NOT_FOUND));
        WorkoutEnvironment environment =
                workoutEnvironmentRepository.findByIdAndMemberId(requestDto.getEnvironmentId(),
                                                                 memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        List<Exercise> eligibleExercises = hardFilterService.getCustomEligibleExercises(memberId,
                                                                                        requestDto.getEnvironmentId());

        if (eligibleExercises.isEmpty()) {
            throw new CustomException(ErrorCode.NO_ELIGIBLE_EXERCISE);
        }
        if (eligibleExercises.size() < 3) {
            throw new CustomException(ErrorCode.INSUFFICIENT_CUSTOM_EXERCISE_CANDIDATES);
        }

        AiCustomExerciseUserProfileDto aiUserProfileDto = AiCustomExerciseUserProfileDto.of(
                memberProfile,
                environment);

        List<AiEligibleExerciseDto> aiEligibleExerciseDto = eligibleExercises.stream()
                .map(AiEligibleExerciseDto::from)
                .toList();

        return CustomExerciseInputDto.of(aiUserProfileDto, aiEligibleExerciseDto);
    }
}
