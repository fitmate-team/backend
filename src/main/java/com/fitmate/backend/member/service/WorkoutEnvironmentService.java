package com.fitmate.backend.member.service;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.equipment.repository.EquipmentRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import com.fitmate.backend.member.dto.request.GymRequestDto;
import com.fitmate.backend.member.dto.request.PrimaryLocationUpdateRequestDto;
import com.fitmate.backend.member.dto.request.WorkoutEnvironmentUpdateRequestDto;
import com.fitmate.backend.member.dto.response.WorkoutEnvironmentListResponseDto;
import com.fitmate.backend.member.dto.response.WorkoutEnvironmentResponseDto;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.MemberRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutEnvironmentService {
    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final EquipmentRepository equipmentRepository;

    public WorkoutEnvironmentListResponseDto getWorkoutEnvironments(Long memberId) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<WorkoutEnvironmentResponseDto> responseDtos =
                workoutEnvironmentRepository.findAllByMemberId(
                memberId).stream().map(WorkoutEnvironmentResponseDto::from).toList();

        return WorkoutEnvironmentListResponseDto.of(memberProfile.getExerciseLocation(),
                                                    responseDtos);
    }

    @Transactional
    public void updatePrimaryLocation(Long memberId, PrimaryLocationUpdateRequestDto requestDto) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        memberProfile.updateExerciseLocation(requestDto.getLocation());
    }

    @Transactional
    public WorkoutEnvironmentResponseDto updateWorkoutEnvironment(Long memberId,
                                                                  Long environmentId,
                                                                  WorkoutEnvironmentUpdateRequestDto requestDto) {
        WorkoutEnvironment environment = workoutEnvironmentRepository.findByIdAndMemberId(
                        environmentId,
                        memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        // 기구 수정
        if (requestDto.getEquipmentCodes() != null) {
            Set<Equipment> equipmentSet = // 운동 기구 코드로 Equipment 조회
                    equipmentRepository.findAllByEquipmentCodeIn(requestDto.getEquipmentCodes());
            if (requestDto.getEquipmentCodes().size() != equipmentSet.size()) {
                throw new CustomException(ErrorCode.INVALID_EQUIPMENT_CODE); // 개수 유효 검사
            }
            environment.updateEquipment(equipmentSet);
        }

        // 헬스장 이름 수정
        if (environment.getLocationType() == ExerciseLocation.GYM) {
            if (requestDto.getGymName() != null) {
                if (!StringUtils.hasText(requestDto.getGymName())) {
                    throw new CustomException(ErrorCode.INVALID_WORKOUT_ENVIRONMENT);
                }
                environment.updateGymName(requestDto.getGymName().trim());
            }
        } else {
            if (StringUtils.hasText(requestDto.getGymName())) {
                throw new CustomException(ErrorCode.INVALID_WORKOUT_ENVIRONMENT);
            }
        }

        return WorkoutEnvironmentResponseDto.from(environment);
    }

    @Transactional
    public WorkoutEnvironmentResponseDto createGym(Long memberId, GymRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Set<Equipment> equipmentSet = // 운동 기구 코드로 Equipment 조회
                equipmentRepository.findAllByEquipmentCodeIn(requestDto.getEquipmentCodes());
        if (requestDto.getEquipmentCodes().size() != equipmentSet.size()) {
            throw new CustomException(ErrorCode.INVALID_EQUIPMENT_CODE); // 개수 유효 검사
        }

        WorkoutEnvironment savedWorkoutEnvironment =
                workoutEnvironmentRepository.save(requestDto.toWorkoutEnvironment(
                member,
                equipmentSet));

        return WorkoutEnvironmentResponseDto.from(savedWorkoutEnvironment);
    }

}