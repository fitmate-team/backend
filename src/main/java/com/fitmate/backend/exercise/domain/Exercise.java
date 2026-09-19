package com.fitmate.backend.exercise.domain;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.member.domain.enums.BodyArea;
import com.fitmate.backend.member.domain.enums.ExerciseLevel;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
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
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String exerciseCode;

    @Column(nullable = false)
    private String nameKo;

    @Column(nullable = false)
    private String nameEn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExerciseType exerciseType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PrimaryMuscle primaryMuscle;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MovementPattern movementPattern;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExerciseLevel minimumExperienceLevel;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExerciseBaselineType baselineRecordType;

    @Column(nullable = false)
    @Lob
    private String guide;

    @Column(nullable = false)
    private String videoUrl;

    @ElementCollection
    @CollectionTable(name = "exercise_related_body_areas", joinColumns = @JoinColumn(name =
            "exercise_id"))
    @Column(name = "body_area", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<BodyArea> relatedBodyAreas = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "exercise_equipment", joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private Set<Equipment> equipment = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "exercise_available_locations", joinColumns = @JoinColumn(name =
            "exercise_id"))
    @Column(name = "location_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<ExerciseLocation> availableLocations = new HashSet<>();

    @Builder
    public Exercise(String exerciseCode,
                    String nameKo,
                    String nameEn,
                    ExerciseType exerciseType,
                    PrimaryMuscle primaryMuscle,
                    MovementPattern movementPattern,
                    ExerciseLevel minimumExperienceLevel,
                    ExerciseBaselineType baselineRecordType,
                    String guide,
                    String videoUrl,
                    Set<BodyArea> relatedBodyAreas,
                    Set<Equipment> equipment,
                    Set<ExerciseLocation> availableLocations) {

        this.exerciseCode = exerciseCode;
        this.nameKo = nameKo;
        this.nameEn = nameEn;
        this.exerciseType = exerciseType;
        this.primaryMuscle = primaryMuscle;
        this.movementPattern = movementPattern;
        this.minimumExperienceLevel = minimumExperienceLevel;
        this.baselineRecordType = baselineRecordType;
        this.guide = guide;
        this.videoUrl = videoUrl;

        this.relatedBodyAreas =
                relatedBodyAreas != null ? new HashSet<>(relatedBodyAreas) : new HashSet<>();

        this.equipment = equipment != null ? new HashSet<>(equipment) : new HashSet<>();

        this.availableLocations =
                availableLocations != null ? new HashSet<>(availableLocations) : new HashSet<>();
    }
}
