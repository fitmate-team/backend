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
public class BodyMeasurement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Double weight;
    private Double skeletalMuscleMass;
    private Double bodyFatPercentage;
    private Double bodyFatMass;

    @Builder
    public BodyMeasurement(Member member,
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
