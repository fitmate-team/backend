package com.fitmate.backend.exercise.repository;

import com.fitmate.backend.exercise.domain.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    Set<Exercise> findAllByExerciseCodeIn(Set<String> exerciseCodes);
    Optional<Exercise> findByExerciseCode(String exerciseCode);
}
