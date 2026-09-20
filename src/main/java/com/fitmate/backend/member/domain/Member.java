package com.fitmate.backend.member.domain;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "member_excluded_exercise", joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "exercise_id"))
    private Set<Exercise> excludedExercises = new HashSet<>();

    @Builder
    public Member(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }

    public void updateExcludedExercises(Set<Exercise> excludedExercises) {
        this.excludedExercises.clear();

        if (excludedExercises != null) {
            this.excludedExercises.addAll(excludedExercises);
        }
    }

}
