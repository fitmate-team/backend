package com.fitmate.backend.member.domain;

import com.fitmate.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 자동으로 id 지정해줌
    private Long id;

    @Column(nullable = false, unique = true, length = 16) // DB 레벨 제약
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    private String nickname;

    @Builder
    public Member(String loginId,
                  String password,
                  String nickname) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
    }

}
