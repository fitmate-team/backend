package com.fitmate.backend.exercise.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import com.fitmate.backend.exercise.dto.ExerciseResponseDto;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

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
}
