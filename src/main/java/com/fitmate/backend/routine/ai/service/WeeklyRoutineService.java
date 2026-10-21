package com.fitmate.backend.routine.ai.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.*;
import com.fitmate.backend.member.repository.BodyCompositionRepository;
import com.fitmate.backend.member.repository.ExerciseBaselineRepository;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.ai.prompt.AiPromptBuilder;
import com.fitmate.backend.routine.ai.dto.input.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.input.AiRecentExerciseRecordDto;
import com.fitmate.backend.routine.ai.dto.input.WeeklyGenerationInputDto;
import com.fitmate.backend.routine.ai.dto.input.AiUserProfileDto;
import com.fitmate.backend.routine.ai.dto.request.WeeklyGenerationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyRoutineService {
    private final MemberProfileRepository memberProfileRepository;
    private final BodyCompositionRepository bodyCompositionRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseBaselineRepository exerciseBaselineRepository;
    private final HardFilterService hardFilterService;
    private final AiPromptBuilder aiPromptBuilder;

    public String createAiPrompt(Long memberId, WeeklyGenerationRequestDto requestDto) {

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
        List<DayOfWeek> effectiveDays = requestDto.getSelectedDays()
                .stream()
                .filter(d -> d.getValue() >= today.getDayOfWeek().getValue())
                .distinct()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .toList(); // 오늘 이전 요일 제외 및 중복 제거
        if (effectiveDays.isEmpty()) {
            throw new CustomException(ErrorCode.NO_AVAILABLE_ROUTINE_DAY); // 아예 비었으면 루틴 생성 금지
        }

        List<ExerciseBaseline> baselines = exerciseBaselineRepository.findAllByMemberId(memberId);

        List<Exercise> eligibleExercises = hardFilterService.getEligibleExercises(memberId,
                                                                                  requestDto.getEnvironmentId(),
                                                                                  weekStartDate);
        if (eligibleExercises.isEmpty()) {
            throw new CustomException(ErrorCode.NO_ELIGIBLE_EXERCISE); // 아예 비었으면 루틴 생성 금지
        }
        // 프론트에서 넘어온 값 기준으로 AI 입력 프로필 생성 (마이페이지 X)
        AiUserProfileDto aiUserProfileDto = AiUserProfileDto.of(memberProfile,
                                                                composition,
                                                                environment,
                                                                effectiveDays.size(),
                                                                effectiveDays,
                                                                requestDto.getSessionMinutes());
        List<AiEligibleExerciseDto> aiEligibleExerciseDtos = eligibleExercises.stream()
                .map(AiEligibleExerciseDto::from)
                .toList();
        List<AiRecentExerciseRecordDto> aiRecentExerciseRecordDtos = baselines.stream()
                .map(AiRecentExerciseRecordDto::from)
                .toList();


        WeeklyGenerationInputDto weeklyGenerationInputDto = WeeklyGenerationInputDto.of(aiUserProfileDto,
                                                                                        aiEligibleExerciseDtos,
                                                                                        aiRecentExerciseRecordDtos);
        return aiPromptBuilder.build(weeklyGenerationInputDto);
    }

}