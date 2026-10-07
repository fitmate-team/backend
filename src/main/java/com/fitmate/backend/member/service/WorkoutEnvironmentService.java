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
import com.fitmate.backend.member.dto.request.EquipmentUpdateRequestDto;
import com.fitmate.backend.member.dto.response.WorkoutEnvironmentListResponseDto;
import com.fitmate.backend.member.dto.response.WorkoutEnvironmentResponseDto;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.MemberRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public WorkoutEnvironmentResponseDto updateEquipment(Long memberId,
                                                         ExerciseLocation locationType,
                                                         EquipmentUpdateRequestDto requestDto) {
        WorkoutEnvironment workoutEnvironment =
                workoutEnvironmentRepository.findByMemberIdAndLocationType(
                                memberId,
                                locationType)
                        .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        Set<Equipment> equipmentSet = // 운동 기구 코드로 Equipment 조회
                equipmentRepository.findAllByEquipmentCodeIn(requestDto.getEquipmentCodes());
        if (requestDto.getEquipmentCodes().size() != equipmentSet.size()) {
            throw new CustomException(ErrorCode.INVALID_EQUIPMENT_CODE); // 개수 유효 검사
        }

        workoutEnvironment.updateEquipment(equipmentSet);
        return WorkoutEnvironmentResponseDto.from(workoutEnvironment);
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

        WorkoutEnvironment workoutEnvironment = WorkoutEnvironment.builder()
                .member(member)
                .gymName(requestDto.getGymName())
                .locationType(ExerciseLocation.GYM)
                .equipment(equipmentSet)
                .build();

        WorkoutEnvironment savedWorkoutEnvironment = workoutEnvironmentRepository.save(
                workoutEnvironment);

        return WorkoutEnvironmentResponseDto.from(savedWorkoutEnvironment);
    }

    @Transactional
    public WorkoutEnvironmentResponseDto updateGym(Long memberId,
                                                   Long environmentId,
                                                   GymRequestDto requestDto) {

        WorkoutEnvironment workoutEnvironment =
                workoutEnvironmentRepository.findByIdAndMemberId(environmentId, memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        Set<Equipment> equipmentSet = // 운동 기구 코드로 Equipment 조회
                equipmentRepository.findAllByEquipmentCodeIn(requestDto.getEquipmentCodes());
        if (requestDto.getEquipmentCodes().size() != equipmentSet.size()) {
            throw new CustomException(ErrorCode.INVALID_EQUIPMENT_CODE); // 개수 유효 검사
        }

        workoutEnvironment.updateGym(requestDto.getGymName(), equipmentSet);
        return WorkoutEnvironmentResponseDto.from(workoutEnvironment);
    }
}