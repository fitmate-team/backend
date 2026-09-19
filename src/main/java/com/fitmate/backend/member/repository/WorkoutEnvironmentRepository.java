package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutEnvironmentRepository extends JpaRepository<WorkoutEnvironment, Long> {
    List<WorkoutEnvironment> findAllByMemberId(Long memberId);
}
