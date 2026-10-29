package com.fitmate.backend.exercise.repository;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.member.domain.enums.ExerciseLocation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    Set<Exercise> findAllByExerciseCodeIn(Set<String> exerciseCodes);
    Optional<Exercise> findByExerciseCode(String exerciseCode);

    /**
     * 지정한 운동 장소에서 가능한 운동 목록을 조회합니다.
     * 운동별 기구 분류를 확인할 수 있도록 기구 정보를 함께 조회합니다.
     * 보유 기구에 따른 운동 가능 여부는 서비스에서 추가로 판단합니다.
     */
    @EntityGraph(attributePaths = {"equipment"})
    @Query("""
        select distinct exercise
        from Exercise exercise
        where :locationType member of exercise.availableLocations
        order by exercise.id asc
        """)
    List<Exercise> findAvailableByLocationType(
            @Param("locationType") ExerciseLocation locationType
    );
}
