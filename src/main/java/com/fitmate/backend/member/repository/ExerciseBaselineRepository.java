package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.ExerciseBaseline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseBaselineRepository extends JpaRepository<ExerciseBaseline, Long> {

    List<ExerciseBaseline> findAllByMemberId(Long memberId);
    void deleteAllByMemberId(Long memberId);}
