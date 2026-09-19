package com.fitmate.backend.member.service;

import com.fitmate.backend.auth.token.RefreshTokenRepository;
import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.equipment.repository.EquipmentRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.BodyWeight;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import com.fitmate.backend.member.dto.request.BodyMetricsUpdateRequestDto;
import com.fitmate.backend.member.dto.request.MemberProfileUpdateRequestDto;
import com.fitmate.backend.member.dto.request.SignUpRequestDto;
import com.fitmate.backend.member.dto.response.LoginIdCheckResponseDto;
import com.fitmate.backend.member.dto.response.MemberResponseDto;
import com.fitmate.backend.member.dto.response.SignUpResponseDto;
import com.fitmate.backend.member.repository.BodyWeightRepository;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.MemberRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final BodyWeightRepository bodyWeightRepository;
    private final EquipmentRepository equipmentRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto requestDto) {
        if (memberRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        Member member = requestDto.toMember(passwordEncoder.encode(requestDto.getPassword()));
        Member savedMember = memberRepository.save(member);

        memberProfileRepository.save(requestDto.toMemberProfile(savedMember));
        bodyWeightRepository.save(requestDto.toBodyWeight(savedMember));

        Set<Equipment> equipmentSet = // 운동 기구 ID 조회
                equipmentRepository.findAllByEquipmentCodeIn(requestDto.getEquipmentCodes());

        if (requestDto.getEquipmentCodes().size() != equipmentSet.size()) {
            throw new CustomException(ErrorCode.INVALID_EQUIPMENT_CODE); // 유효 코드 검증
        }

        boolean defaultGym = requestDto.getExerciseLocation() == ExerciseLocation.GYM;

        workoutEnvironmentRepository.save(requestDto.toWorkoutEnvironment(savedMember,
                                                                          defaultGym,
                                                                          equipmentSet));
        return SignUpResponseDto.from(savedMember);
    }

    public LoginIdCheckResponseDto checkLoginId(String loginId) {
        boolean exists = memberRepository.existsByLoginId(loginId);
        return new LoginIdCheckResponseDto(!exists);
    }

    public MemberResponseDto getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        BodyWeight bodyWeight = bodyWeightRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member, memberProfile, bodyWeight);
    }

    @Transactional
    public MemberResponseDto updateMemberProfile(Long memberId,
                                                 MemberProfileUpdateRequestDto requestDto) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        memberProfile.updateMemberProfile(requestDto.getGender(),
                                          requestDto.getAge(),
                                          requestDto.getHeight(),
                                          requestDto.getExerciseLevel(),
                                          requestDto.getCurrentExerciseStatus(),
                                          requestDto.getPrimaryGoal(),
                                          requestDto.getGoalStrategy(),
                                          requestDto.getWeeklyFrequency(),
                                          requestDto.getSessionMinutes(),
                                          requestDto.getExerciseLocation(),
                                          requestDto.getAvailableDays(),
                                          requestDto.getAvoidBodyAreas());

        Member member = memberProfile.getMember();

        BodyWeight bodyWeight = bodyWeightRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member, memberProfile, bodyWeight);

    }

    @Transactional
    public void updateBodyMetrics(Long memberId, BodyMetricsUpdateRequestDto requestDto) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        memberProfile.updateBodyMetrics(requestDto.getSkeletalMuscleMass(),
                                        requestDto.getBodyFatPercentage(),
                                        requestDto.getBodyFatMass());

        if (requestDto.getWeight() != null) {
            bodyWeightRepository.save(BodyWeight.builder()
                                              .member(memberProfile.getMember())
                                              .weight(requestDto.getWeight())
                                              .build());
        }

    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        refreshTokenRepository.deleteByMemberId(memberId);
        bodyWeightRepository.deleteAllByMemberId(memberId);
        memberProfileRepository.deleteByMemberId(memberId);
        memberRepository.delete(member);
    }
}
