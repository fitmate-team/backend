package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.MemberRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HardFilterService {

}
