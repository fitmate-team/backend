package com.fitmate.backend.member.domain;

import com.fitmate.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @org.hibernate.annotations.ColumnDefault("true")
    @Column(name = "ai_adjustment_enabled", nullable = false)
    private boolean aiAdjustmentEnabled = true;

    @org.hibernate.annotations.ColumnDefault("true")
    @Column(name = "difficulty_question_enabled", nullable = false)
    private boolean difficultyQuestionEnabled = true;

    @org.hibernate.annotations.ColumnDefault("true")
    @Column(name = "recommendation_reason_visible", nullable = false)
    private boolean recommendationReasonVisible = true;

    @org.hibernate.annotations.ColumnDefault("false")
    @Column(name = "auto_apply_enabled", nullable = false)
    private boolean autoApplyEnabled;

    @Builder
    public MemberSettings(Member member,
                          Boolean aiAdjustmentEnabled,
                          Boolean difficultyQuestionEnabled,
                          Boolean recommendationReasonVisible,
                          Boolean autoApplyEnabled) {
        this.member = member;
        this.aiAdjustmentEnabled = aiAdjustmentEnabled != null ? aiAdjustmentEnabled : true;
        this.difficultyQuestionEnabled = difficultyQuestionEnabled != null ? difficultyQuestionEnabled : true;
        this.recommendationReasonVisible = recommendationReasonVisible != null ? recommendationReasonVisible : true;
        this.autoApplyEnabled = autoApplyEnabled != null ? autoApplyEnabled : false;
    }
}