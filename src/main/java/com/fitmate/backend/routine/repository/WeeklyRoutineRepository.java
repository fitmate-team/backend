package com.fitmate.backend.routine.repository;

import com.fitmate.backend.routine.domain.WeeklyRoutine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Date;

public interface WeeklyRoutineRepository extends JpaRepository<WeeklyRoutine, Long> {
    boolean existsByMember_IdAndWeekStartDate(Long id, LocalDate weekStartDate);
}
