package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
    Optional<MemberProfile> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
