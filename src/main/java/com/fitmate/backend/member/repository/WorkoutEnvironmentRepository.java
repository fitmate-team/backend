package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutEnvironmentRepository extends JpaRepository<WorkoutEnvironment, Long> {
    List<WorkoutEnvironment> findAllByMemberId(Long memberId);

    Optional<WorkoutEnvironment> findByIdAndMemberId(Long id, Long memberId);

    void deleteAllByMemberId(Long memberId);

}
