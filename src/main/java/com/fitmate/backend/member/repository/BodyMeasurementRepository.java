package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.BodyMeasurement;
import com.fitmate.backend.member.domain.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {
    Optional<BodyMeasurement> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);
}
