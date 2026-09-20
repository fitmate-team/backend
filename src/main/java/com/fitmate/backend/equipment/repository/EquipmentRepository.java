package com.fitmate.backend.equipment.repository;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Set<Equipment> findAllByEquipmentCodeIn(Set<String> equipmentCodes);
    Set<Equipment> findAllByAvailableLocationsContaining(ExerciseLocation location);
}