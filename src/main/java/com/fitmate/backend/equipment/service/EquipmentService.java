package com.fitmate.backend.equipment.service;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.equipment.dto.EquipmentResponseDto;
import com.fitmate.backend.equipment.repository.EquipmentRepository;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;

    public List<EquipmentResponseDto> getEquipmentsByLocation(ExerciseLocation location) {
        Set<Equipment> equipments =
                equipmentRepository.findAllByAvailableLocationsContaining(location);

        return equipments.stream()
                .map(EquipmentResponseDto::from)
                .toList();
    }
}
