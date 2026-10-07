package com.fitmate.backend.member.domain;

import com.fitmate.backend.global.common.BaseEntity;
import com.fitmate.backend.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "body_composition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BodyComposition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Double weight;

    @Column(name = "skeletal_muscle_mass")
    private Double skeletalMuscleMass;

    @Column(name = "body_fat_percentage")
    private Double bodyFatPercentage;

    @Column(name = "body_fat_mass")
    private Double bodyFatMass;

    @Builder
    public BodyComposition(Member member,
                           Double weight,
                           Double skeletalMuscleMass,
                           Double bodyFatPercentage,
                           Double bodyFatMass) {
        this.member = member;
        this.weight = weight;
        this.skeletalMuscleMass = skeletalMuscleMass;
        this.bodyFatPercentage = bodyFatPercentage;
        this.bodyFatMass = bodyFatMass;
    }
}