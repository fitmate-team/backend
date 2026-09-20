package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.BodyWeight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodyWeightRepository extends JpaRepository<BodyWeight, Long> {
    Optional<BodyWeight> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
