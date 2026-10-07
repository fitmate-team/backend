package com.fitmate.backend.exercise.dto;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.equipment.domain.EquipmentCategory;
import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class ExerciseResponseDto {
    private String exerciseCode;
    private String nameKo;
    private ExerciseType exerciseType;
    private ExerciseBaselineType baselineRecordType;
    private PrimaryMuscle primaryMuscle;
    private Set<EquipmentCategory> equipmentCategories;

    public static ExerciseResponseDto from(Exercise exercise) {
        return new ExerciseResponseDto(exercise.getExerciseCode(),
                                       exercise.getNameKo(),
                                       exercise.getExerciseType(),
                                       exercise.getBaselineRecordType(),
                                       exercise.getPrimaryMuscle(),
                                       exercise.getEquipment()
                                               .stream()
                                               .map(Equipment::getCategory)
                                               .collect(Collectors.toSet())
                                       );
    }
}
