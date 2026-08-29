package com.fitmate.backend.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // 토큰 생성
    public String generateAccessToken(Long memberId) {
        // 현재 시간
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);

        // 만료 시간
        Date expiration = new Date(now + accessTokenExpiration);

        // SecretKey 만들기
        SecretKey key = getSigningKey();

        // JWT 생성
        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    // 클레임 꺼내기
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token) // 검증
                .getPayload();
    }

    // ID 추출
    public Long getMemberId(Claims claims) {
        String id = claims.getSubject();
        return Long.parseLong(id);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
