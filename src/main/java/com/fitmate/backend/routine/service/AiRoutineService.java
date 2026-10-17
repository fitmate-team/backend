package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.*;
import com.fitmate.backend.member.repository.BodyCompositionRepository;
import com.fitmate.backend.member.repository.ExerciseBaselineRepository;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.dto.ai.AiEligibleExerciseDto;
import com.fitmate.backend.routine.dto.ai.AiRecentExerciseRecordDto;
import com.fitmate.backend.routine.dto.ai.AiRoutineRequestDto;
import com.fitmate.backend.routine.dto.ai.AiUserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiRoutineService {
    private final MemberProfileRepository memberProfileRepository;
    private final BodyCompositionRepository bodyCompositionRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseBaselineRepository exerciseBaselineRepository;
    private final HardFilterService hardFilterService;

    public AiRoutineRequestDto createAiRoutineRequest(Long memberId,
                                                      Long environmentId,
                                                      LocalDate weekStartDate) {

        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        BodyComposition composition =
                bodyCompositionRepository.findTopByMemberIdOrderByCreatedAtDesc(
                memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        WorkoutEnvironment environment = workoutEnvironmentRepository.findByIdAndMemberId(
                        environmentId,
                        memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        List<Exercise> eligibleExercises = hardFilterService.getEligibleExercises(memberId,
                                                                                  environmentId,
                                                                                  weekStartDate);
        List<ExerciseBaseline> baselines = exerciseBaselineRepository.findAllByMemberId(memberId);

        AiUserProfileDto aiUserProfileDto = AiUserProfileDto.of(memberProfile,
                                                                composition,
                                                                environment);


        List<AiEligibleExerciseDto> aiEligibleExerciseDtos = eligibleExercises.stream()
                .map(AiEligibleExerciseDto::from)
                .toList();

        List<AiRecentExerciseRecordDto> aiRecentExerciseRecordDtos = baselines.stream()
                .map(AiRecentExerciseRecordDto::from)
                .toList();

        return AiRoutineRequestDto.of(aiUserProfileDto,
                                      aiEligibleExerciseDtos,
                                      aiRecentExerciseRecordDtos);
    }
}