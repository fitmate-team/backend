package com.fitmate.backend.routine.ai.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.domain.MemberProfile;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.MemberRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.domain.RoutineExercise;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import com.fitmate.backend.workout.repository.WorkoutExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HardFilterService {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final MemberRepository memberRepository;
    private final WorkoutExerciseRecordRepository workoutExerciseRecordRepository;


    public List<Exercise> getEligibleExercises(Long memberId,
                                               Long environmentId,
                                               LocalDate weekStartDate) {

        WorkoutEnvironment environment = workoutEnvironmentRepository.findByIdAndMemberId(
                        environmentId,
                        memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<Exercise> exercises = exerciseRepository.findAll();

        LocalDate previousWeekStart = weekStartDate.minusDays(7);
        LocalDate previousWeekEnd = weekStartDate.minusDays(1);

        // 전주 운동 기록 조회
        List<WorkoutExerciseRecord> previousWeekExerciseRecords =
                workoutExerciseRecordRepository.findByWorkoutRecord_DailyRoutine_WeeklyRoutine_Member_IdAndWorkoutRecord_DailyRoutine_RoutineDateBetween(
                        memberId,
                        previousWeekStart,
                        previousWeekEnd);

        // 전주 운동 기록 중 실제 수행한 기록
        List<WorkoutExerciseRecord> performedPreviousWeekRecords =
                previousWeekExerciseRecords.stream()
                        .filter(record -> record.getCompletedSetCount() > 0 ||
                                record.getCompletedDurationSeconds() > 0)
                        .toList();

        // 실제 수행한 운동 중 유산소 제거
        // 이번 주 AI 추천에서 사용하면 안되는 운동
        Set<Exercise> excludedPreviousWeekExercises = performedPreviousWeekRecords.stream()
                .map(WorkoutExerciseRecord::getRoutineExercise)
                .map(RoutineExercise::getExercise)
                .filter(exercise -> exercise.getExerciseType() != ExerciseType.CARDIO)
                .collect(Collectors.toSet());

        // AI 추천에 사용할 최종 운동
        return exercises.stream()
                .filter(exercise -> exercise.getAvailableLocations()
                        .contains(environment.getLocationType())) // 장소
                .filter(exercise -> environment.getEquipment()
                        .containsAll(exercise.getEquipment())) // 기구
                .filter(exercise -> memberProfile.getExerciseLevel().getLevel() >=
                        exercise.getMinimumExperienceLevel().getLevel()) // 운동 레벨
                .filter(exercise -> exercise.getRelatedBodyAreas()
                        .stream()
                        .noneMatch(bodyArea -> memberProfile.getAvoidBodyAreas()
                                .contains(bodyArea))) // 회피 부위
                .filter(exercise -> !member.getExcludedExercises().contains(exercise)) // 사용자 제외 운동
                .filter(exercise -> !excludedPreviousWeekExercises.contains(exercise)) // 전주 수행 운동
                .toList();
    }

}
