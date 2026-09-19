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

    private String gymAddress;

    @Column(nullable = false)
    private boolean defaultGym;

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
                              String gymAddress,
                              boolean defaultGym,
                              ExerciseLocation locationType,
                              Set<Equipment> equipment) {
        this.member = member;
        this.gymName = gymName;
        this.gymAddress = gymAddress;
        this.defaultGym = defaultGym;
        this.locationType = locationType;
        this.equipment = equipment != null ? new HashSet<>(equipment) : new HashSet<>();
    }
}
