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

    @Column(nullable = false, unique = true, length = 15) // DB 레벨 제약
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExerciseLevel exerciseLevel;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExerciseGoal exerciseGoal;

    private Double targetWeight;

    @Builder
    public Member(String loginId,
                  String password,
                  String nickname,
                  Gender gender,
                  Double height,
                  Double weight,
                  ExerciseLevel exerciseLevel,
                  ExerciseGoal exerciseGoal,
                  Double targetWeight) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.exerciseLevel = exerciseLevel;
        this.exerciseGoal = exerciseGoal;
        this.targetWeight = targetWeight;
    }

    public void updateMember(String nickname,
                             Gender gender,
                             Double height,
                             Double weight,
                             ExerciseLevel exerciseLevel,
                             ExerciseGoal exerciseGoal,
                             Double targetWeight){
        this.nickname = nickname;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.exerciseLevel = exerciseLevel;
        this.exerciseGoal = exerciseGoal;
        this.targetWeight = targetWeight;
    }

}
