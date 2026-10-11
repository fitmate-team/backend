package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutEnvironmentRepository extends JpaRepository<WorkoutEnvironment, Long> {
    List<WorkoutEnvironment> findAllByMemberId(Long memberId);

    Optional<WorkoutEnvironment> findByIdAndMemberId(Long id, Long memberId);

    void deleteAllByMemberId(Long memberId);

    // 운동 환경과 해당 환경에 등록된 운동 기구를 함께 조회
    @EntityGraph(attributePaths = {"equipment"})
    Optional<WorkoutEnvironment> findWithEquipmentByIdAndMemberId(
            Long id,
            Long memberId
    );
}
