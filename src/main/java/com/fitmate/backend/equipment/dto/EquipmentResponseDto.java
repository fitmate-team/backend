package com.fitmate.backend.equipment.dto;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.equipment.domain.EquipmentCategory;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EquipmentResponseDto {
    private String equipmentCode;
    private String nameKo;
    private EquipmentCategory category;

    public static EquipmentResponseDto from(Equipment equipment) {
        return new EquipmentResponseDto(equipment.getEquipmentCode(),
                                        equipment.getNameKo(),
                                        equipment.getCategory());
    }
}
