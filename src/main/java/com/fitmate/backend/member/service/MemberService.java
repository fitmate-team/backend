package com.fitmate.backend.member.service;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.dto.SignUpRequestDto;
import com.fitmate.backend.member.dto.SignUpResponseDto;
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

    @Transactional
    public SignUpResponseDto createMember(SignUpRequestDto requestDto) {
        if (memberRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        Member member = requestDto.toEntity(passwordEncoder.encode(requestDto.getPassword()));
        memberRepository.save(member);
        return SignUpResponseDto.from(member);
    }

//    public SignUpResponseDto getMember() {
//
//    }
//
//    public SignUpResponseDto updateMember(SignUpRequestDto requestDto) {
//    }
//
//    public SignUpResponseDto deleteMember(SignUpRequestDto requestDto) {
//    }
}
