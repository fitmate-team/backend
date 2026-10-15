package com.fitmate.backend.exercise.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import com.fitmate.backend.exercise.dto.ExerciseGuideResponseDto;
import com.fitmate.backend.exercise.dto.ExerciseResponseDto;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import com.fitmate.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Locale.filter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;

    public List<ExerciseResponseDto> getExercises(String keyword, PrimaryMuscle muscle) {

        return exerciseRepository.findAll()
                .stream()
                .filter(exercise -> !StringUtils.hasText(keyword) ||
                        exercise.getNameKo().contains(keyword.trim()))
                .filter(exercise -> muscle == null || exercise.getPrimaryMuscle() == muscle)
                .map(ExerciseResponseDto::from)
                .toList();
    }

    public List<ExerciseGuideResponseDto> getExerciseGuides(
            List<Long> exerciseIds
    ) {
        // 중복 ID 제거 및 요청 순서 유지
        Set<Long> uniqueIds = new LinkedHashSet<>(exerciseIds);

        if (uniqueIds.isEmpty() || uniqueIds.contains(null)) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_IDS);
        }

        // 요청한 운동만 DB에서 조회
        List<Exercise> exercises = exerciseRepository.findAllById(uniqueIds);

        // 존재하지 않는 운동 ID 검증
        if (exercises.size() != uniqueIds.size()) {
            throw new CustomException(ErrorCode.EXERCISE_NOT_FOUND);
        }

        // 운동 ID를 기준으로 매핑
        Map<Long, Exercise> exerciseMap = exercises.stream()
                .collect(Collectors.toMap(
                        Exercise::getId,
                        Function.identity()
                ));

        // 요청한 ID 순서대로 응답
        return uniqueIds.stream()
                .map(exerciseMap::get)
                .map(ExerciseGuideResponseDto::from)
                .toList();
    }
}
