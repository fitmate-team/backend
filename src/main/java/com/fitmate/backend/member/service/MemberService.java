package com.fitmate.backend.member.service;

import com.fitmate.backend.auth.token.RefreshTokenRepository;
import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.equipment.repository.EquipmentRepository;
import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.*;
import com.fitmate.backend.member.dto.request.MemberProfileUpdateRequestDto;
import com.fitmate.backend.member.dto.request.RecentExerciseRecordRequestDto;
import com.fitmate.backend.member.dto.request.SignUpRequestDto;
import com.fitmate.backend.member.dto.response.LoginIdCheckResponseDto;
import com.fitmate.backend.member.dto.response.MemberResponseDto;
import com.fitmate.backend.member.dto.response.SignUpResponseDto;
import com.fitmate.backend.member.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final BodyCompositionRepository bodyCompositionRepository;
    private final EquipmentRepository equipmentRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseBaselineRepository exerciseBaselineRepository;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto requestDto) {
        if (memberRepository.existsByLoginId(requestDto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        Member member = requestDto.toMember(passwordEncoder.encode(requestDto.getPassword()));
        Member savedMember = memberRepository.save(member);

        memberProfileRepository.save(requestDto.toMemberProfile(savedMember));
        bodyCompositionRepository.save(requestDto.toBodyComposition(savedMember));

        Set<Equipment> equipmentSet = // 운동 기구 코드로 Equipment 조회
                equipmentRepository.findAllByEquipmentCodeIn(requestDto.getEquipmentCodes());
        if (requestDto.getEquipmentCodes().size() != equipmentSet.size()) {
            throw new CustomException(ErrorCode.INVALID_EQUIPMENT_CODE); // 개수 유효 검사
        }
        workoutEnvironmentRepository.save(requestDto.toWorkoutEnvironment(savedMember, equipmentSet));

        Set<Exercise> excludedExerciseSet = // 제외 운동 코드로 Exercise 조회
                exerciseRepository.findAllByExerciseCodeIn(requestDto.getExcludedExerciseCodes());
        if (requestDto.getExcludedExerciseCodes().size() != excludedExerciseSet.size()) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_CODE); // 개수 유효 검사
        }
        savedMember.updateExcludedExercises(excludedExerciseSet);

        Set<String> exerciseCodes = requestDto.getRecentExerciseRecords()
                .stream()
                .map(RecentExerciseRecordRequestDto::getExerciseCode)
                .collect(Collectors.toSet()); // 최근 운동 기록 코드 수집
        if (requestDto.getRecentExerciseRecords().size() != exerciseCodes.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_RECENT_EXERCISE);
        }

        Set<Exercise> exercises = exerciseRepository.findAllByExerciseCodeIn(exerciseCodes);
        if (exerciseCodes.size() != exercises.size()) {
            throw new CustomException(ErrorCode.INVALID_EXERCISE_CODE);
        }

        Map<String, Exercise> exerciseMap = exercises.stream()
                .collect(Collectors.toMap(Exercise::getExerciseCode, e -> e));

        List<ExerciseBaseline> exerciseBaselines = new ArrayList<>();
        for (RecentExerciseRecordRequestDto record : requestDto.getRecentExerciseRecords()) {
            Exercise exercise = exerciseMap.get(record.getExerciseCode());

            ExerciseBaselineType type = exercise.getBaselineRecordType();

            if (type == ExerciseBaselineType.WEIGHT_REPS_SETS &&
                    (record.getWeight() == null ||
                            record.getReps() == null ||
                            record.getSets() == null ||
                            record.getDurationSeconds() != null)) {

                throw new CustomException(ErrorCode.INVALID_EXERCISE_BASELINE);

            } else if (type == ExerciseBaselineType.REPS_SETS &&
                    (record.getWeight() != null ||
                            record.getReps() == null ||
                            record.getSets() == null ||
                            record.getDurationSeconds() != null)) {

                throw new CustomException(ErrorCode.INVALID_EXERCISE_BASELINE);

            } else if (type == ExerciseBaselineType.DURATION &&
                    (record.getWeight() != null ||
                            record.getReps() != null ||
                            record.getSets() != null ||
                            record.getDurationSeconds() == null)) {

                throw new CustomException(ErrorCode.INVALID_EXERCISE_BASELINE);
            }

            ExerciseBaseline exerciseBaseline = ExerciseBaseline.builder()
                    .member(savedMember)
                    .exercise(exercise)
                    .weight(record.getWeight())
                    .reps(record.getReps())
                    .sets(record.getSets())
                    .durationSeconds(record.getDurationSeconds())
                    .build();

            exerciseBaselines.add(exerciseBaseline);
        }

        exerciseBaselineRepository.saveAll(exerciseBaselines);

        return SignUpResponseDto.from(savedMember);
    }

    public LoginIdCheckResponseDto checkLoginId(String loginId) {
        boolean exists = memberRepository.existsByLoginId(loginId);
        return new LoginIdCheckResponseDto(!exists);
    }

    public MemberResponseDto getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        BodyComposition bodyComposition = bodyCompositionRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member, memberProfile, bodyComposition);
    }

    @Transactional
    public MemberResponseDto updateMemberProfile(Long memberId,
                                                 MemberProfileUpdateRequestDto requestDto) {
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        memberProfile.updateMemberProfile(requestDto.getGender(),
                                          requestDto.getAge(),
                                          requestDto.getHeight(),
                                          requestDto.getExerciseLevel(),
                                          requestDto.getCurrentExerciseStatus(),
                                          requestDto.getPrimaryGoal(),
                                          requestDto.getGoalStrategy(),
                                          requestDto.getWeeklyFrequency(),
                                          requestDto.getSessionMinutes(),
                                          requestDto.getExerciseLocation(),
                                          requestDto.getAvailableDays(),
                                          requestDto.getAvoidBodyAreas());

        Member member = memberProfile.getMember();

        BodyComposition bodyComposition = bodyCompositionRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.from(member, memberProfile, bodyComposition);

    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        refreshTokenRepository.deleteByMemberId(memberId);
        bodyCompositionRepository.deleteAllByMemberId(memberId);
        workoutEnvironmentRepository.deleteAllByMemberId(memberId);
        exerciseBaselineRepository.deleteAllByMemberId(memberId);
        memberProfileRepository.deleteByMemberId(memberId);


        memberRepository.delete(member);
    }
}
