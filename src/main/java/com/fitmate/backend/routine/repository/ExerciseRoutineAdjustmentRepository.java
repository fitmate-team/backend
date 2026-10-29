package com.fitmate.backend.routine.repository;

import com.fitmate.backend.routine.domain.ExerciseRoutineAdjustment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRoutineAdjustmentRepository
        extends JpaRepository<ExerciseRoutineAdjustment, Long> {

    // 회원과 운동에 해당하는 조정값을 조회한다.
    Optional<ExerciseRoutineAdjustment> findByMember_IdAndExercise_Id(
            Long memberId,
            Long exerciseId
    );

    // 회원과 운동에 해당하는 조정값을 삭제한다.
    void deleteByMember_IdAndExercise_Id(
            Long memberId,
            Long exerciseId
    );


    // 회원이 보유중인 모든 대기 조정값을 조회함
    List<ExerciseRoutineAdjustment> findAllByMember_Id(Long memberId);

}