package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutEnvironmentRepository extends JpaRepository<Member, Long> {
}
