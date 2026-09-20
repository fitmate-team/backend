package com.fitmate.backend.equipment.domain;

import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String equipmentCode;

    @Column(nullable = false)
    private String nameKo;

    @Column(nullable = false)
    private String nameEn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EquipmentCategory category;

    @ElementCollection
    @CollectionTable(name = "equipment_available_locations", joinColumns = @JoinColumn(name =
            "equipment_id"))
    @Column(name = "location_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<ExerciseLocation> availableLocations = new HashSet<>();

}
