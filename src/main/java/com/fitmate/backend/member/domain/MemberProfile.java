package com.fitmate.backend.member.domain;

import com.fitmate.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 자동으로 id 지정해줌
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExerciseLevel exerciseLevel;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrentExerciseStatus currentExerciseStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PrimaryGoal primaryGoal;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GoalStrategy goalStrategy;

    @Column(nullable = false)
    private Integer weeklyFrequency;

    @Column(nullable = false)
    private Integer sessionMinutes;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExerciseLocation exerciseLocation;

    @ElementCollection
    @CollectionTable(name = "member_profile_available_days", joinColumns = @JoinColumn(name =
            "member_profile_id"))
    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> availableDays = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "member_profile_avoid_body_areas", joinColumns = @JoinColumn(name =
            "member_profile_id"))
    @Column(name = "body_area", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<BodyArea> avoidBodyAreas = new HashSet<>();

}
