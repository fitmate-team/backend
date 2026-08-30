package com.fitmate.backend.auth.service;

import com.fitmate.backend.auth.dto.request.LoginRequestDto;
import com.fitmate.backend.auth.dto.response.LoginResponseDto;
import com.fitmate.backend.auth.token.RefreshToken;
import com.fitmate.backend.auth.token.RefreshTokenRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.global.security.jwt.JwtTokenProvider;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.hierarchicalroles.CycleInRoleHierarchyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {
        Member member = memberRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId());

        Optional<RefreshToken> optionalRefreshToken =
                refreshTokenRepository.findByMemberId(member.getId());

        // 리프레시 토큰 있으면 update, 없으면 새로 저장
        if (optionalRefreshToken.isPresent()) {
            optionalRefreshToken.get().updateToken(refreshToken);
        }else{
            refreshTokenRepository.save(RefreshToken.builder()
                                                .memberId(member.getId())
                                                .refreshToken(refreshToken)
                                                .build());
        }


        return LoginResponseDto.of(member, accessToken, refreshToken);
    }
}
