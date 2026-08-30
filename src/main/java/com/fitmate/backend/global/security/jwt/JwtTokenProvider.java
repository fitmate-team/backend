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

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // access 생성
    public String generateAccessToken(Long memberId) {
        return generateToken(memberId, accessTokenExpiration);
    }

    // refresh 생성
    public String generateRefreshToken(Long memberId) {
        return generateToken(memberId, refreshTokenExpiration);
    }

    private String generateToken(Long memberId, long expirationTime) {
        // 현재 시간
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);

        // 만료 시간
        Date expiration = new Date(now + expirationTime);

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
