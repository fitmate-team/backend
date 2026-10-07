package com.fitmate.backend.auth.token;

import com.fitmate.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: FK 적용
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false, length = 500)
    private String refreshToken;

    // TODO: 객체 매개변수를 받기로 생성자 변경
    @Builder
    public RefreshToken(Member member, String refreshToken) {
        this.member = member;
        this.refreshToken = refreshToken;
    }

    // Update
    public void updateToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
