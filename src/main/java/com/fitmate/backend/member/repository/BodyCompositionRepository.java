package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.BodyComposition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodyCompositionRepository extends JpaRepository<BodyComposition, Long> {
    Optional<BodyComposition> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
