package com.fitmate.backend.equipment.repository;

import com.fitmate.backend.equipment.domain.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Set<Equipment> findAllByEquipmentCodeIn(Set<String> equipmentCodes);
}