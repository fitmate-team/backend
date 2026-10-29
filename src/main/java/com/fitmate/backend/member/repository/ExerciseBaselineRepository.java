package com.fitmate.backend.member.repository;

import com.fitmate.backend.member.domain.ExerciseBaseline;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ExerciseBaselineRepository extends JpaRepository<ExerciseBaseline, Long> {

    List<ExerciseBaseline> findAllByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);

    /**
     * 선택한 운동들의 회원 기준 기록 일괄 조회
     * - 해당 회원의 기록만 조회
     * - 요청한 운동 코드에 해당하는 기준 기록만 반환
     */
    @EntityGraph(attributePaths = {"exercise"})
    List<ExerciseBaseline> findByMember_IdAndExercise_ExerciseCodeIn(
            Long memberId,
            Collection<String> exerciseCodes
    );

}


