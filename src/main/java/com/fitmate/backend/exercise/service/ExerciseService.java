package com.fitmate.backend.exercise.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.dto.ExerciseResponseDto;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;

    public List<ExerciseResponseDto> getExercises() {
        List<Exercise> exercises = exerciseRepository.findAll();

        return exercises.stream()
                .map(ExerciseResponseDto::from)
                .toList();
    }
}
