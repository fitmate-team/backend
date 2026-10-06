package com.fitmate.backend.member.domain;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import com.fitmate.backend.member.domain.enums.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
// TODO (수정 완료): (member_id, gym_name) 복합 UNIQUE 추가
@Table(name = "workout_environment", uniqueConstraints = @UniqueConstraint(
        name = "uk_workout_environment_member_gym", columnNames = {"member_id", "gym_name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutEnvironment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String gymName;

    // TODO (수정 완료): 헬스장 주소, 기본 헬스장 및 생성자 관련 코드 삭제
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExerciseLocation locationType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "workout_environment_equipment", joinColumns = @JoinColumn(name =
            "workout_environment_id"), inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private Set<Equipment> equipment = new HashSet<>();

    @Builder
    public WorkoutEnvironment(Member member,
                              String gymName,
                              ExerciseLocation locationType,
                              Set<Equipment> equipment) {
        this.member = member;
        this.gymName = gymName;
        this.locationType = locationType;
        this.equipment = equipment != null ? new HashSet<>(equipment) : new HashSet<>();
    }
}
