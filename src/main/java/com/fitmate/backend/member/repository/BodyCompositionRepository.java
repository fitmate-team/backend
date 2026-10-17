package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.BodyComposition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BodyCompositionRepository extends JpaRepository<BodyComposition, Long> {
    Optional<BodyComposition> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    void deleteAllByMemberId(Long memberId);

    boolean existsByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long memberId,
            LocalDateTime startOfDay,
            LocalDateTime startOfNextDay
    );

    Optional<BodyComposition>
    findTopByMember_IdAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(
            Long memberId,
            LocalDateTime now
    );

    Optional<BodyComposition>
    findTopByMember_IdAndSkeletalMuscleMassIsNotNullAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(
            Long memberId,
            LocalDateTime now
    );

    Optional<BodyComposition>
    findTopByMember_IdAndBodyFatPercentageIsNotNullAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(
            Long memberId,
            LocalDateTime now
    );

    Optional<BodyComposition>
    findTopByMember_IdAndBodyFatMassIsNotNullAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(
            Long memberId,
            LocalDateTime now
    );
}
