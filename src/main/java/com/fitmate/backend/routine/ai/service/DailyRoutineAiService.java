package com.fitmate.backend.routine.ai.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.BodyComposition;
import com.fitmate.backend.member.domain.ExerciseBaseline;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.repository.BodyCompositionRepository;
import com.fitmate.backend.member.repository.ExerciseBaselineRepository;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.ai.client.OpenAiClient;
import com.fitmate.backend.routine.ai.dto.input.*;
import com.fitmate.backend.routine.ai.dto.output.DailyRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.request.DailyRoutineRequestDto;
import com.fitmate.backend.routine.ai.prompt.AiPromptBuilder;
import com.fitmate.backend.routine.ai.validator.DailyRoutineValidator;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyRoutineAiService {
    private final MemberProfileRepository memberProfileRepository;
    private final BodyCompositionRepository bodyCompositionRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseBaselineRepository exerciseBaselineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final WorkoutRecordRepository workoutRecordRepository;
    private final HardFilterService hardFilterService;
    private final AiPromptBuilder aiPromptBuilder;
    private final OpenAiClient openAiClient;
    private final DailyRoutineValidator validator;

    public String createAiPrompt(Long memberId, DailyRoutineRequestDto requestDto) {
        DailyRoutineInputDto input = buildGenerationInput(memberId, requestDto);
        return aiPromptBuilder.build(input);
    }

    public DailyRoutineOutputDto generateDailyRoutine(Long memberId,
                                                      DailyRoutineRequestDto requestDto) {
        DailyRoutineInputDto input = buildGenerationInput(memberId, requestDto);
        String prompt = aiPromptBuilder.build(input);
        DailyRoutineOutputDto output = openAiClient.generate(prompt,
                                                             DailyRoutineOutputDto.class);
        validator.validate(input, output);
        return output;
    }

    private DailyRoutineInputDto buildGenerationInput(Long memberId,
                                                      DailyRoutineRequestDto requestDto) {
        // DB 조회
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        BodyComposition composition =
                bodyCompositionRepository.findTopByMemberIdOrderByCreatedAtDesc(
                memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        WorkoutEnvironment environment =
                workoutEnvironmentRepository.findByIdAndMemberId(requestDto.getEnvironmentId(),
                                                                                          memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        DailyRoutine dailyRoutine =
                dailyRoutineRepository.findByWeeklyRoutine_Member_IdAndRoutineDate(
                memberId,
                today).orElseThrow(() -> new CustomException(ErrorCode.TODAY_ROUTINE_NOT_FOUND));

        if (workoutRecordRepository.existsByDailyRoutine_Id(dailyRoutine.getId())) {
            throw new CustomException(ErrorCode.WORKOUT_ALREADY_STARTED);
        }

        List<ExerciseRoutine> exerciseRoutine =
                exerciseRoutineRepository.findByDailyRoutine_IdOrderByExerciseOrderAsc(
                dailyRoutine.getId());

        List<ExerciseBaseline> baselines = exerciseBaselineRepository.findAllByMemberId(memberId);
        List<Exercise> eligibleExercises = hardFilterService.getEligibleExercises(memberId,
                                                                                  requestDto.getEnvironmentId(),
                                                                                  weekStartDate);
        if (eligibleExercises.isEmpty()) {
            throw new CustomException(ErrorCode.NO_ELIGIBLE_EXERCISE); // 아예 비었으면 루틴 생성 금지
        }

        // 입력 DTO 구성 (USER_PROFILE, ELIGIBLE_EXERCISES, RECENT_EXERCISE_RECORDS, ORIGINAL_EXERCISES)
        AiUserProfileDto aiUserProfileDto = AiUserProfileDto.of(memberProfile,
                                                                composition,
                                                                environment,
                                                                1,
                                                                List.of(today.getDayOfWeek()),
                                                                requestDto.getSessionMinutes());
        List<AiEligibleExerciseDto> aiEligibleExerciseDtos = eligibleExercises.stream()
                .map(AiEligibleExerciseDto::from)
                .toList();
        List<AiRecentExerciseRecordDto> aiRecentExerciseRecordDtos = baselines.stream()
                .map(AiRecentExerciseRecordDto::from)
                .toList();
        List<AiDailyOriginalExerciseDto> originalExerciseDtos = exerciseRoutine.stream()
                .map(AiDailyOriginalExerciseDto::from)
                .toList();

        return DailyRoutineInputDto.of(aiUserProfileDto,
                                       aiEligibleExerciseDtos,
                                       aiRecentExerciseRecordDtos,
                                       originalExerciseDtos);
    }
}
