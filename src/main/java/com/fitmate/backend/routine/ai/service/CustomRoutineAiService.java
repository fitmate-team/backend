package com.fitmate.backend.routine.ai.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseType;
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
import com.fitmate.backend.routine.ai.dto.custom.summary.AiCustomSummaryExerciseDto;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomAiSummaryRequestDto;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomSummaryInputDto;
import com.fitmate.backend.routine.ai.dto.custom.summary.CustomSummaryOutputDto;
import com.fitmate.backend.routine.ai.prompt.AiPromptBuilder;
import com.fitmate.backend.routine.ai.validator.CustomExerciseValidator;
import com.fitmate.backend.routine.ai.validator.CustomPrescriptionValidator;
import com.fitmate.backend.routine.ai.validator.CustomSummaryValidator;
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
    private final CustomSummaryValidator customSummaryValidator;

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

    public CustomSummaryOutputDto generateSummary(Long memberId,
                                                  CustomAiSummaryRequestDto requestDto) {
        CustomSummaryInputDto input = buildSummaryInput(memberId, requestDto);
        String prompt = aiPromptBuilder.build(input);
        CustomSummaryOutputDto output = openAiClient.generate(prompt, CustomSummaryOutputDto.class);
        customSummaryValidator.validate(input, output);
        return output;
    }

    private CustomSummaryInputDto buildSummaryInput(Long memberId,
                                                    CustomAiSummaryRequestDto requestDto) {

        List<CustomAiSummaryRequestDto.ExerciseDto> requestExercises = requestDto.getExercises();

        // 요청 운동 코드 추출
        Set<String> exerciseCodes = requestExercises.stream()
                .map(CustomAiSummaryRequestDto.ExerciseDto::getExerciseCode)
                .collect(Collectors.toSet());

        // Exercise Master에서 운동 조회
        Set<Exercise> exercises = exerciseRepository.findAllByExerciseCodeIn(exerciseCodes);

        if (exercises.size() != exerciseCodes.size()) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_CODE);
        }

        // 운동 코드로 Exercise 조회할 수 있도록 Map 생성
        Map<String, Exercise> exerciseMap = exercises.stream()
                .collect(Collectors.toMap(Exercise::getExerciseCode, Function.identity()));

        // 운동별 처방 검증 및 AI 입력 DTO 생성
        List<AiCustomSummaryExerciseDto> summaryExercises = requestExercises.stream().map(dto -> {

            Exercise exercise = exerciseMap.get(dto.getExerciseCode());

            if (exercise == null) {
                throw new CustomException(ErrorCode.INVALID_EXERCISE_CODE);
            }

            // 공통 처방 수치 검증
            if (dto.getSets() == null || dto.getSets() <= 0 || dto.getRestSeconds() == null ||
                    dto.getRestSeconds() < 0 ||
                    (dto.getWeightKg() != null && dto.getWeightKg().signum() <= 0) ||
                    (dto.getReps() != null && dto.getReps() <= 0) ||
                    (dto.getDurationSeconds() != null && dto.getDurationSeconds() <= 0)) {

                throw new CustomException(ErrorCode.INVALID_CUSTOM_ROUTINE_PRESCRIPTION);
            }

            // 운동 유형 정보 검증
            if (exercise.getBaselineRecordType() == null || exercise.getExerciseType() == null) {
                throw new CustomException(ErrorCode.INVALID_CUSTOM_ROUTINE_PRESCRIPTION);
            }

            // 기록 유형별 처방 형식 검증
            boolean invalidPrescription = switch (exercise.getBaselineRecordType()) {

                case WEIGHT_REPS_SETS ->
                        dto.getReps() == null || dto.getDurationSeconds() != null ||
                                exercise.getExerciseType() == ExerciseType.CARDIO;
                case REPS_SETS -> dto.getReps() == null || dto.getWeightKg() != null ||
                        dto.getDurationSeconds() != null ||
                        exercise.getExerciseType() == ExerciseType.CARDIO;
                case DURATION -> exercise.getExerciseType() != ExerciseType.CARDIO ||
                        dto.getDurationSeconds() == null || dto.getReps() != null ||
                        dto.getWeightKg() != null || dto.getSets() != 1 ||
                        dto.getRestSeconds() != 0;
            };

            if (invalidPrescription) {
                throw new CustomException(ErrorCode.INVALID_CUSTOM_ROUTINE_PRESCRIPTION);
            }

            // 검증된 운동을 AI 입력 DTO로 변환
            return AiCustomSummaryExerciseDto.from(exercise, dto);
        }).toList();

        return CustomSummaryInputDto.of(summaryExercises);
    }
}
