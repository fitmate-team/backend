package com.fitmate.backend.member.service;

import com.fitmate.backend.auth.token.RefreshTokenRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.ExerciseGoal;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.dto.request.MemberUpdateRequestDto;
import com.fitmate.backend.member.dto.request.SignUpRequestDto;
import com.fitmate.backend.member.dto.response.LoginIdCheckResponseDto;
import com.fitmate.backend.member.dto.response.MemberResponseDto;
import com.fitmate.backend.member.dto.response.SignUpResponseDto;
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

    @Transactional
    public SignUpResponseDto createMember(SignUpRequestDto requestDto) {
        if (memberRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        Member member = requestDto.toEntity(passwordEncoder.encode(requestDto.getPassword()));
        Member savedMember = memberRepository.save(member);
        return SignUpResponseDto.from(savedMember);
    }

    public LoginIdCheckResponseDto checkLoginId(String loginId) {
        boolean exists = memberRepository.existsByLoginId(loginId);
        return new LoginIdCheckResponseDto(!exists);
    }

    public MemberResponseDto getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member);
    }

    @Transactional
    public MemberResponseDto updateMember(Long memberId, MemberUpdateRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (requestDto.getExerciseGoal() == ExerciseGoal.DIET &&
                requestDto.getTargetWeight() == null) {
            throw new CustomException(ErrorCode.TARGET_WEIGHT_REQUIRED);
        }

        member.updateMember
                (requestDto.getNickname(),
                 requestDto.getGender(),
                 requestDto.getHeight(),
                 requestDto.getWeight(),
                 requestDto.getExerciseLevel(),
                 requestDto.getExerciseGoal(),
                 requestDto.getTargetWeight()
                );

        return MemberResponseDto.from(member);

    }

    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        refreshTokenRepository.deleteByMemberId(memberId);
        memberRepository.delete(member);
    }
}
