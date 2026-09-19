package com.fitmate.backend.member.service;

import com.fitmate.backend.auth.token.RefreshTokenRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.BodyMeasurement;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.dto.request.MemberProfileUpdateRequestDto;
import com.fitmate.backend.member.dto.request.SignUpRequestDto;
import com.fitmate.backend.member.dto.response.LoginIdCheckResponseDto;
import com.fitmate.backend.member.dto.response.MemberResponseDto;
import com.fitmate.backend.member.dto.response.SignUpResponseDto;
import com.fitmate.backend.member.repository.BodyMeasurementRepository;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto requestDto) {
        if (memberRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        Member member = requestDto.toMember(passwordEncoder.encode(requestDto.getPassword()));
        Member savedMember = memberRepository.save(member);

        memberProfileRepository.save(requestDto.toMemberProfile(savedMember));
        bodyMeasurementRepository.save(requestDto.toBodyMeasurement(savedMember));

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

        BodyMeasurement bodyMeasurement =
                bodyMeasurementRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member, memberProfile, bodyMeasurement);
    }

    @Transactional
    public MemberResponseDto updateMember(Long memberId, MemberProfileUpdateRequestDto requestDto) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        memberProfile.updateMemberProfile(
                requestDto.getGender(),
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
                requestDto.getAvoidBodyAreas()
        );

        Member member = memberProfile.getMember();

        BodyMeasurement bodyMeasurement =
                bodyMeasurementRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member, memberProfile, bodyMeasurement);

    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        refreshTokenRepository.deleteByMemberId(memberId);
        memberRepository.delete(member);
    }
}
