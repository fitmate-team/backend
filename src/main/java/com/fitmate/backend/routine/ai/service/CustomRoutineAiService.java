package com.fitmate.backend.routine.ai.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.ExerciseBaseline;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.repository.ExerciseBaselineRepository;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.ai.client.OpenAiClient;
import com.fitmate.backend.routine.ai.dto.common.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.AiCustomExerciseUserProfileDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomAiExerciseRequestDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomExerciseInputDto;
import com.fitmate.backend.routine.ai.dto.custom.exercise.CustomExerciseOutputDto;
import com.fitmate.backend.routine.ai.dto.custom.prescription.*;
import com.fitmate.backend.routine.ai.prompt.AiPromptBuilder;
import com.fitmate.backend.routine.ai.validator.CustomExerciseValidator;
import com.fitmate.backend.routine.ai.validator.CustomPrescriptionValidator;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import com.fitmate.backend.workout.repository.WorkoutExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomRoutineAiService {
    private final MemberProfileRepository memberProfileRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutExerciseRecordRepository workoutExerciseRecordRepository;
    private final ExerciseBaselineRepository exerciseBaselineRepository;
    private final HardFilterService hardFilterService;
    private final AiPromptBuilder aiPromptBuilder;
    private final OpenAiClient openAiClient;
    private final CustomExerciseValidator customExerciseValidator;
    private final CustomPrescriptionValidator customPrescriptionValidator;

    public CustomExerciseOutputDto generateExerciseRecommendations(Long memberId,
                                                                   CustomAiExerciseRequestDto requestDto) {
        CustomExerciseInputDto input = buildGenerationInput(memberId, requestDto);
        String prompt = aiPromptBuilder.build(input);
        CustomExerciseOutputDto output = openAiClient.generate(prompt,
                                                               CustomExerciseOutputDto.class);
        customExerciseValidator.validate(input, output);
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


    public CustomPrescriptionResponseDto generatePrescriptionRecommendations(Long memberId,
                                                                             CustomAiPrescriptionRequestDto requestDto) {

        CustomPrescriptionInputDto input = buildPrescriptionInput(memberId, requestDto);

        // Recommendation 없음
        if (input.getExercises().isEmpty()) {

            List<CustomPrescriptionResponseDto.ExerciseRecommendationDto> exercises =
                    new ArrayList<>();

            for (String exerciseCode : requestDto.getExerciseCodes()) {
                exercises.add(CustomPrescriptionResponseDto.ExerciseRecommendationDto.of(
                        exerciseCode,
                        null));
            }

            return CustomPrescriptionResponseDto.of(exercises);
        }

        String prompt = aiPromptBuilder.build(input);
        CustomPrescriptionOutputDto output = openAiClient.generate(prompt,
                                                                   CustomPrescriptionOutputDto.class);
        customPrescriptionValidator.validate(input, output);

        // Response 구현
        // 처방 Map 변환 (코드:처방값)
        Map<String, CustomPrescriptionOutputDto.PrescriptionDto> prescriptionMap =
                output.getPrescriptions()
                .stream()
                .collect(Collectors.toMap(CustomPrescriptionOutputDto.PrescriptionDto::getExerciseCode,
                                          Function.identity()));

        List<CustomPrescriptionResponseDto.ExerciseRecommendationDto> exercises = new ArrayList<>();

        for (String exerciseCode : requestDto.getExerciseCodes()) {

            CustomPrescriptionOutputDto.PrescriptionDto prescription = prescriptionMap.get(
                    exerciseCode);

            CustomPrescriptionResponseDto.RecommendationDto recommendation;

            if (prescription != null) {
                recommendation = CustomPrescriptionResponseDto.RecommendationDto.from(prescription);
            } else {
                recommendation = null;
            }

            exercises.add(CustomPrescriptionResponseDto.ExerciseRecommendationDto.of(exerciseCode,
                                                                                     recommendation));
        }
        return CustomPrescriptionResponseDto.of(exercises);
    }


    private CustomPrescriptionInputDto buildPrescriptionInput(Long memberId,
                                                              CustomAiPrescriptionRequestDto requestDto) {

        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_PROFILE_NOT_FOUND));

        // 요청한 운동 코드 확인
        List<String> exerciseCodes = requestDto.getExerciseCodes();

        Set<String> uniqueCodes = new HashSet<>(exerciseCodes);

        if (uniqueCodes.size() != exerciseCodes.size()) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_CODE);
        }

        // 선택한 운동 조회
        Set<Exercise> exercises = exerciseRepository.findAllByExerciseCodeIn(uniqueCodes);

        if (exercises.size() != uniqueCodes.size()) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_CODE);
        }

        // 운동 코드를 Key로 Exercise 조회
        Map<String, Exercise> exerciseMap = exercises.stream()
                .collect(Collectors.toMap(Exercise::getExerciseCode, Function.identity()));

        // 최근 실제 수행 기록 조회
        Map<String, List<AiCustomPrescriptionRecordDto>> recentRecordsMap = new HashMap<>();

        List<String> noRecordCodes = new ArrayList<>();

        for (String exerciseCode : exerciseCodes) {

            List<WorkoutExerciseRecord> records =
                    workoutExerciseRecordRepository.findRecentPerformedRecordsByMemberIdAndExerciseCode(
                    memberId,
                    exerciseCode,
                    PageRequest.of(0, 3));

            if (records.isEmpty()) {
                noRecordCodes.add(exerciseCode);
            } else {
                List<AiCustomPrescriptionRecordDto> recordDtos = records.stream()
                        .map(AiCustomPrescriptionRecordDto::from)
                        .toList();

                recentRecordsMap.put(exerciseCode, recordDtos);
            }
        }

        // 실제 수행 기록이 없는 운동의 Baseline 조회
        Map<String, ExerciseBaseline> baselineMap = new HashMap<>();

        if (!noRecordCodes.isEmpty()) {

            List<ExerciseBaseline> baselines =
                    exerciseBaselineRepository.findByMember_IdAndExercise_ExerciseCodeIn(
                    memberId,
                    noRecordCodes);

            for (ExerciseBaseline baseline : baselines) {
                baselineMap.put(baseline.getExercise().getExerciseCode(), baseline);
            }
        }

        // AI에 전달할 운동별 DTO 생성
        List<AiCustomPrescriptionExerciseDto> prescriptionExercises = new ArrayList<>();

        for (String exerciseCode : exerciseCodes) {

            Exercise exercise = exerciseMap.get(exerciseCode);
            List<AiCustomPrescriptionRecordDto> recentRecords = recentRecordsMap.get(exerciseCode);

            // 실제 수행 기록이 있는 운동
            if (recentRecords != null && !recentRecords.isEmpty()) {

                prescriptionExercises.add(AiCustomPrescriptionExerciseDto.from(exercise,
                                                                               recentRecords,
                                                                               null));
                continue;
            }

            // 실제 수행 기록이 없으면 Baseline 확인
            ExerciseBaseline baseline = baselineMap.get(exerciseCode);

            if (baseline != null) {
                AiCustomPrescriptionBaselineDto baselineDto = AiCustomPrescriptionBaselineDto.from(
                        baseline);

                prescriptionExercises.add(AiCustomPrescriptionExerciseDto.from(exercise,
                                                                               List.of(),
                                                                               baselineDto));
            }
            // 둘 다 없는 운동은 AI 입력에서 제외
        }

        // 회원 정보 DTO 생성
        AiCustomPrescriptionUserProfileDto userProfileDto = AiCustomPrescriptionUserProfileDto.of(
                memberProfile);

        // AI 입력 DTO 반환
        return CustomPrescriptionInputDto.of(userProfileDto, prescriptionExercises);
    }


}
