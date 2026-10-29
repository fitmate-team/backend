package com.fitmate.backend.routine.ai.validator;

import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.routine.ai.dto.custom.prescription.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CustomPrescriptionValidator {

    public void validate(CustomPrescriptionInputDto input, CustomPrescriptionOutputDto output) {

        // 1. AI 입력 및 출력 목록 검사
        if (input == null || input.getExercises() == null || input.getExercises().isEmpty() ||
                output == null || output.getPrescriptions() == null) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        List<AiCustomPrescriptionExerciseDto> inputExercises = input.getExercises();

        List<CustomPrescriptionOutputDto.PrescriptionDto> prescriptions = output.getPrescriptions();

        // 2. 입력 운동 개수와 출력 처방 개수 일치
        if (inputExercises.size() != prescriptions.size()) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        Set<String> seenCodes = new HashSet<>();

        for (int i = 0; i < inputExercises.size(); i++) {

            AiCustomPrescriptionExerciseDto exercise = inputExercises.get(i);

            CustomPrescriptionOutputDto.PrescriptionDto prescription = prescriptions.get(i);

            // 3. 운동 코드, 순서 및 중복 검사
            if (exercise == null || prescription == null || exercise.getExerciseCode() == null ||
                    !exercise.getExerciseCode().equals(prescription.getExerciseCode()) ||
                    !seenCodes.add(prescription.getExerciseCode())) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            // 4. 운동별 처방값 검사
            validateFormat(exercise, prescription);
        }
    }

    private void validateFormat(AiCustomPrescriptionExerciseDto exercise,
                                CustomPrescriptionOutputDto.PrescriptionDto prescription) {

        // 1. 공통 수치 검사
        if (prescription.getSets() == null || prescription.getSets() <= 0 ||
                prescription.getRestSeconds() == null || prescription.getRestSeconds() < 0 ||
                (prescription.getWeightKg() != null && prescription.getWeightKg() <= 0) ||
                (prescription.getReps() != null && prescription.getReps() <= 0) ||
                (prescription.getDurationSeconds() != null &&
                        prescription.getDurationSeconds() <= 0)) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        // 2. CARDIO는 별도 형식 우선 적용
        if (exercise.getExerciseType() == ExerciseType.CARDIO) {

            if (prescription.getSets() != 1 || prescription.getReps() != null ||
                    prescription.getWeightKg() != null ||
                    prescription.getDurationSeconds() == null ||
                    prescription.getRestSeconds() != 0) {
                throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
            }

            return;
        }

        // 3. 운동 기록 유형 검사
        if (exercise.getBaselineRecordType() == null) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }

        boolean invalidFormat = switch (exercise.getBaselineRecordType()) {

            case WEIGHT_REPS_SETS ->
                    prescription.getReps() == null || prescription.getDurationSeconds() != null ||
                            (hasWeightReference(exercise) != (prescription.getWeightKg() != null));

            case REPS_SETS ->
                    prescription.getReps() == null || prescription.getWeightKg() != null ||
                            prescription.getDurationSeconds() != null;

            case DURATION -> prescription.getReps() != null || prescription.getWeightKg() != null ||
                    prescription.getDurationSeconds() == null;
        };

        if (invalidFormat) {
            throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
        }
    }

    private boolean hasWeightReference(AiCustomPrescriptionExerciseDto exercise) {

        List<AiCustomPrescriptionRecordDto> records = exercise.getRecentRecords();

        // 실제 수행 기록이 있으면 해당 기록의 계획 중량만 확인
        if (records != null && !records.isEmpty()) {

            return records.stream()
                    .anyMatch(record -> record != null && record.getPlannedWeightKg() != null &&
                            record.getPlannedWeightKg() > 0);
        }

        // 실제 수행 기록이 없을 때만 Baseline 확인
        AiCustomPrescriptionBaselineDto baseline = exercise.getBaseline();

        return baseline != null && baseline.getWeightKg() != null && baseline.getWeightKg() > 0;
    }
}
