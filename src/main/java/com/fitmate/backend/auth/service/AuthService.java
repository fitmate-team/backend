package com.fitmate.backend.auth.service;

import com.fitmate.backend.auth.dto.LoginRequestDto;
import com.fitmate.backend.auth.dto.LoginResponseDto;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.global.security.jwt.JwtTokenProvider;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDto login(LoginRequestDto requestDto) {
        Member member = memberRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())){
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getId());
        return LoginResponseDto.of(member, accessToken);
    }
}
