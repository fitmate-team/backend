package com.fitmate.backend.exercise.domain;

import com.fitmate.backend.member.domain.BodyArea;
import com.fitmate.backend.member.domain.ExerciseLevel;
import jakarta.persistence.*;
import jakarta.websocket.Encoder;
import lombok.AccessLevel;
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

    @ElementCollection
    @CollectionTable(name = "exercise_related_body_areas", joinColumns = @JoinColumn(name =
            "exercise_id"))
    @Column(name = "body_area", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<BodyArea> relatedBodyAreas = new HashSet<>();

    @Column(nullable = false)
    @Lob
    private String guide;

    @Column(nullable = false)
    private String videoUrl;
}
