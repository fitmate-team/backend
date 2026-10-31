package com.fitmate.backend.routine.ai.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.domain.ExerciseType;
import com.fitmate.backend.exercise.domain.PrimaryMuscle;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.*;
import com.fitmate.backend.member.repository.BodyCompositionRepository;
import com.fitmate.backend.member.repository.ExerciseBaselineRepository;
import com.fitmate.backend.member.repository.MemberProfileRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.ai.client.OpenAiClient;
import com.fitmate.backend.routine.ai.dto.common.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.common.AiRecentExerciseRecordDto;
import com.fitmate.backend.routine.ai.dto.common.AiUserProfileDto;
import com.fitmate.backend.routine.ai.dto.weekly.WeeklyAiRoutineRequestDto;
import com.fitmate.backend.routine.ai.dto.weekly.WeeklyRoutineInputDto;
import com.fitmate.backend.routine.ai.dto.weekly.WeeklyRoutineOutputDto;
import com.fitmate.backend.routine.ai.prompt.AiPromptBuilder;
import com.fitmate.backend.routine.ai.validator.WeeklyRoutineValidator;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.routine.service.WeeklyRoutineRegenerationService;
import com.fitmate.backend.workout.domain.WorkoutExerciseRecord;
import com.fitmate.backend.workout.repository.WorkoutExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyRoutineRegenerationAiService {

    // 주간 루틴 AI 생성에 필요한 Repository와 Service
    private final MemberProfileRepository memberProfileRepository;
    private final BodyCompositionRepository bodyCompositionRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final ExerciseBaselineRepository exerciseBaselineRepository;
    private final HardFilterService hardFilterService;
    private final WeeklyAdjustmentApplyService weeklyAdjustmentApplyService;
    private final AiPromptBuilder aiPromptBuilder;
    private final OpenAiClient openAiClient;
    private final WeeklyRoutineValidator validator;

    // 재생성 날짜 검사와 기존 운동 기록 조회에 필요한 Service와 Repository
    private final WeeklyRoutineRegenerationService weeklyRoutineRegenerationService;
    private final WorkoutExerciseRecordRepository workoutExerciseRecordRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;


    // 이번주 남은 날짜의 주간 루틴을 AI로 재생성 (DB 저장 X)
    public WeeklyRoutineOutputDto regenerateWeeklyRoutine(Long memberId,
                                                          WeeklyAiRoutineRequestDto requestDto) {

        // AI에 넘길 회원 정보와 추천 가능한 운동 목록 준비
        WeeklyRoutineInputDto input = buildRegenerationInput(memberId, requestDto);

        // 주간 루틴 생성 프롬프트로 AI 호출
        String prompt = aiPromptBuilder.build(input);
        WeeklyRoutineOutputDto output = openAiClient.generate(prompt, WeeklyRoutineOutputDto.class);

        // AI 결과 검사 (요일, 운동 코드, 처방, 운동 시간 등)
        validator.validate(input, output);

        // 검증 끝난 루틴에 대기 중인 운동 처방 조정값 반영 (아직 조정값 소비 X)
        weeklyAdjustmentApplyService.applyPendingAdjustments(memberId,
                                                             output,
                                                             requestDto.getSessionMinutes());

        // AI가 새로 만든 루틴 반환 (아직 DB에 저장하지 않음)
        return output;
    }


    // 재생성에 사용할 회원 정보와 운동 후보를 AI 입력 데이터로 구성
    private WeeklyRoutineInputDto buildRegenerationInput(Long memberId,
                                                         WeeklyAiRoutineRequestDto requestDto) {

        // 회원 프로필, 최신 신체 기록, 선택한 운동 환경 조회
        MemberProfile memberProfile = memberProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_PROFILE_NOT_FOUND));

        BodyComposition composition =
                bodyCompositionRepository.findTopByMemberIdOrderByCreatedAtDesc(
                memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        WorkoutEnvironment environment =
                workoutEnvironmentRepository.findByIdAndMemberId(requestDto.getEnvironmentId(),
                                                                                          memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        // 오늘 날짜 기반 이번주 월요일과 일요일 찾기
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        // 재생성할 요일 검사 (지난 날짜, 운동 기록 있는 날짜, 중복 요일, 운동 진행중인 상태)
        weeklyRoutineRegenerationService.validateSelectedDays(memberId,
                                                              requestDto.getSelectedDays());

        // 선택한 요일을 월-일 순서로 정렬
        List<DayOfWeek> effectiveDays = requestDto.getSelectedDays()
                .stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .toList();

        // 회원이 입력한 운동 기준 기록(Baseline) 조회
        List<ExerciseBaseline> baselines = exerciseBaselineRepository.findAllByMemberId(memberId);

        // 주간 루틴 자동 생성에 사용할 기본 운동 후보 조회
        // 장소, 기구, 운동 수준, 회피 부위, 제외 운동, 전주 수행 운동 조건 검사
        List<Exercise> baseEligibleExercises = hardFilterService.getEligibleExercises(memberId,
                                                                                      requestDto.getEnvironmentId(),
                                                                                      weekStartDate);

        // 추천 가능한 운동이 하나도 없으면 예외
        if (baseEligibleExercises.isEmpty()) {
            throw new CustomException(ErrorCode.NO_ELIGIBLE_EXERCISE);
        }

        // 이번주 운동별 실제 수행 기록 전부 조회
        List<WorkoutExerciseRecord> weeklyExerciseRecords =
                workoutExerciseRecordRepository.findByWorkoutRecord_DailyRoutine_WeeklyRoutine_Member_IdAndWorkoutRecord_DailyRoutine_RoutineDateBetween(
                memberId,
                weekStartDate,
                weekEndDate);

        // 실제로 운동한 기록만 추출 (운동 종료 && (완료 세트 > 0 || 완료 시간 > 0))
        List<WorkoutExerciseRecord> performedExerciseRecords = weeklyExerciseRecords.stream()
                .filter(record -> record.getWorkoutRecord().isFinished())
                .filter(record -> (record.getCompletedSetCount() != null &&
                        record.getCompletedSetCount() > 0) ||
                        (record.getCompletedDurationSeconds() != null &&
                                record.getCompletedDurationSeconds() > 0))
                .toList();

        // 실제 운동한 주동근만 추출 (유산소 제외, 전신 운동은 독립 부위로 처리)
        Set<PrimaryMuscle> performedPrimaryMuscles = performedExerciseRecords.stream()
                .map(record -> record.getExerciseRoutine().getExercise().getPrimaryMuscle())
                .filter(muscle -> muscle != PrimaryMuscle.CARDIO)
                .collect(Collectors.toSet());

        // 이미 지난 날짜나 운동 기록이 있어서 보호해야 하는 일일 루틴 가져옴
        List<DailyRoutine> protectedDailyRoutines =
                weeklyRoutineRegenerationService.getProtectedDailyRoutines(
                memberId);

        // 보호 날짜에 이미 들어간 비유산소 운동 코드 추출 (실제 운동했는지와 상관없음)
        Set<String> protectedExerciseCodes = protectedDailyRoutines.stream()
                .flatMap(dailyRoutine -> exerciseRoutineRepository.findByDailyRoutine_IdOrderByExerciseOrderAsc(
                        dailyRoutine.getId()).stream())
                .map(ExerciseRoutine::getExercise)
                .filter(exercise -> exercise.getExerciseType() != ExerciseType.CARDIO)
                .map(Exercise::getExerciseCode)
                .collect(Collectors.toSet());

        // 기본 운동 후보에서 이번주 실제 운동한 부위와 보호 날짜에 이미 들어간 운동 제외
        List<Exercise> eligibleExercises = baseEligibleExercises.stream()
                .filter(exercise -> !performedPrimaryMuscles.contains(exercise.getPrimaryMuscle()))
                .filter(exercise -> !protectedExerciseCodes.contains(exercise.getExerciseCode()))
                .toList();

        // 이번주 수행 부위와 중복 운동까지 제외했는데 추천 가능한 운동이 없으면 예외
        if (eligibleExercises.isEmpty()) {
            throw new CustomException(ErrorCode.NO_ELIGIBLE_EXERCISE);
        }

        // 회원 정보와 재생성 조건을 AI용 프로필로 변환 (운동일 수는 새로 만들 요일 기준)
        AiUserProfileDto aiUserProfileDto = AiUserProfileDto.of(memberProfile,
                                                                composition,
                                                                environment,
                                                                effectiveDays.size(),
                                                                effectiveDays,
                                                                requestDto.getSessionMinutes());

        // 추천 가능한 운동 목록을 AI용 DTO로 변환
        List<AiEligibleExerciseDto> aiEligibleExerciseDtos = eligibleExercises.stream()
                .map(AiEligibleExerciseDto::from)
                .toList();

        // 운동 기준 기록을 AI용 DTO로 변환
        List<AiRecentExerciseRecordDto> aiRecentExerciseRecordDtos = baselines.stream()
                .map(AiRecentExerciseRecordDto::from)
                .toList();

        // 회원 정보, 운동 후보, 운동 기준 기록을 하나로 묶어서 반환
        return WeeklyRoutineInputDto.of(aiUserProfileDto,
                                        aiEligibleExerciseDtos,
                                        aiRecentExerciseRecordDtos);
    }
}
