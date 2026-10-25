package com.fitmate.backend.routine.service;

import com.fitmate.backend.exercise.domain.Exercise;
import com.fitmate.backend.exercise.repository.ExerciseRepository;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.ExerciseBaseline;
import com.fitmate.backend.member.domain.WorkoutEnvironment;
import com.fitmate.backend.member.repository.ExerciseBaselineRepository;
import com.fitmate.backend.member.repository.WorkoutEnvironmentRepository;
import com.fitmate.backend.routine.ai.dto.input.AiDailyOriginalExerciseDto;
import com.fitmate.backend.routine.ai.dto.input.AiEligibleExerciseDto;
import com.fitmate.backend.routine.ai.dto.input.AiRecentExerciseRecordDto;
import com.fitmate.backend.routine.ai.dto.output.ExerciseRoutineOutputDto;
import com.fitmate.backend.routine.ai.service.HardFilterService;
import com.fitmate.backend.routine.ai.validator.ExercisePrescriptionValidator;
import com.fitmate.backend.routine.domain.DailyRoutine;
import com.fitmate.backend.routine.domain.ExerciseRoutine;
import com.fitmate.backend.routine.dto.request.DailyRoutineSaveRequestDto;
import com.fitmate.backend.routine.repository.DailyRoutineRepository;
import com.fitmate.backend.routine.repository.ExerciseRoutineRepository;
import com.fitmate.backend.workout.dto.response.TodayWorkoutResponseDto;
import com.fitmate.backend.workout.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyRoutineService {
    private final DailyRoutineRepository dailyRoutineRepository;
    private final ExerciseRoutineRepository exerciseRoutineRepository;
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;
    private final WorkoutRecordRepository workoutRecordRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseBaselineRepository exerciseBaselineRepository;
    private final HardFilterService hardFilterService;
    private final ExercisePrescriptionValidator prescriptionValidator;

    @Transactional
    public void saveRoutine(Long memberId, DailyRoutineSaveRequestDto requestDto) {

        // 오늘의 당일 루틴 조회
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        DailyRoutine dailyRoutine =
                dailyRoutineRepository.findByWeeklyRoutine_Member_IdAndRoutineDate(
                memberId,
                today).orElseThrow(() -> new CustomException(ErrorCode.TODAY_ROUTINE_NOT_FOUND));
        // 이미 운동을 시작했으면 저장 불가
        if (workoutRecordRepository.existsByDailyRoutine_Id(dailyRoutine.getId())) {
            throw new CustomException(ErrorCode.WORKOUT_ALREADY_STARTED);
        }

        // 변경할 운동 환경 조회 및 소유권 확인
        WorkoutEnvironment environment =
                workoutEnvironmentRepository.findByIdAndMemberId(requestDto.getEnvironmentId(),
                                                                                          memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_ENVIRONMENT_NOT_FOUND));

        // 기존 오늘 운동 목록 조회
        List<ExerciseRoutine> originalExercises =
                exerciseRoutineRepository.findByDailyRoutine_IdOrderByExerciseOrderAsc(
                dailyRoutine.getId());

        // 새로운 환경에서 추천 가능한 운동 조회
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<Exercise> eligibleExercises = hardFilterService.getEligibleExercises(memberId,
                                                                                  requestDto.getEnvironmentId(),
                                                                                  weekStartDate);
        if (eligibleExercises.isEmpty()) {
            throw new CustomException(ErrorCode.NO_ELIGIBLE_EXERCISE);
        }

        // 최근 운동 기록 조회
        List<ExerciseBaseline> baselines = exerciseBaselineRepository.findAllByMemberId(memberId);

        // 저장 요청 검증
        validateSaveRequest(requestDto, originalExercises, eligibleExercises, baselines);

        // 기존 오늘 운동 목록 삭제
        exerciseRoutineRepository.deleteAll(originalExercises);
        exerciseRoutineRepository.flush();

        // 9. 재추천된 운동 목록 저장
        for (int i = 0; i < requestDto.getReplacements().size(); i++) {

            DailyRoutineSaveRequestDto.ExerciseDto exerciseDto = requestDto.getReplacements()
                    .get(i)
                    .getReplacementExercise();

            Exercise exercise = exerciseRepository.findByExerciseCode(exerciseDto.getExerciseCode())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EXERCISE_CODE));

            exerciseRoutineRepository.save(exerciseDto.toExerciseRoutine(dailyRoutine,
                                                                         exercise,
                                                                         i + 1));
        }

        // 오늘 루틴의 운동 부위, 환경, 예상 시간 갱신
        dailyRoutine.updateRoutine(requestDto.getMainTarget(),
                                   environment,
                                   requestDto.getEstimatedMinutes());
    }

    private void validateSaveRequest(DailyRoutineSaveRequestDto requestDto,
                                     List<ExerciseRoutine> originalExercises,
                                     List<Exercise> eligibleExercises,
                                     List<ExerciseBaseline> baselines) {

        // 운동 시간 및 개수 검사
        if (requestDto.getEstimatedMinutes() > requestDto.getSessionMinutes()) {
            throw new CustomException(ErrorCode.ROUTINE_DURATION_EXCEEDED);
        }
        List<DailyRoutineSaveRequestDto.ReplacementDto> replacements = requestDto.getReplacements();
        if (originalExercises.isEmpty() || originalExercises.size() != replacements.size()) {
            throw new CustomException(ErrorCode.INVALID_DAILY_ROUTINE_SAVE);
        }

        // 추천 가능한 운동 및 최근 기록
        Map<String, Exercise> eligibleMap = eligibleExercises.stream()
                .collect(Collectors.toMap(Exercise::getExerciseCode, Function.identity()));
        List<AiRecentExerciseRecordDto> recentRecords = baselines.stream()
                .map(AiRecentExerciseRecordDto::from)
                .toList();

        for (int i = 0; i < originalExercises.size(); i++) {
            ExerciseRoutine original = originalExercises.get(i);
            DailyRoutineSaveRequestDto.ReplacementDto replacement = replacements.get(i);
            DailyRoutineSaveRequestDto.ExerciseDto exercise = replacement.getReplacementExercise();

            String originalCode = original.getExercise().getExerciseCode();
            Exercise candidate = eligibleMap.get(exercise.getExerciseCode());

            // 원본 순서, Hard Filter, 운동명, KEEP 규칙 검사
            if (!originalCode.equals(replacement.getOriginalExerciseCode()) || candidate == null ||
                    !candidate.getNameKo().equals(exercise.getNameKo()) ||
                    (eligibleMap.containsKey(originalCode) &&
                            !originalCode.equals(exercise.getExerciseCode()))) {
                throw new CustomException(ErrorCode.INVALID_DAILY_ROUTINE_SAVE);
            }

            // 기존 공통 처방 검증 재사용
            ExerciseRoutineOutputDto output =
                    new ExerciseRoutineOutputDto(exercise.getExerciseCode(),
                                                                           exercise.getNameKo(),
                                                                           exercise.getSets(),
                                                                           exercise.getReps(),
                                                                           exercise.getDurationSeconds(),
                                                                           exercise.getWeightKg(),
                                                                           exercise.getRestSeconds());

            AiEligibleExerciseDto eligible = AiEligibleExerciseDto.from(candidate);

            try {
                prescriptionValidator.validateFormat(output, eligible);
                prescriptionValidator.validateDailyWeight(output,
                                                          eligible,
                                                          recentRecords,
                                                          AiDailyOriginalExerciseDto.from(original));
            } catch (CustomException e) {
                throw new CustomException(ErrorCode.INVALID_DAILY_ROUTINE_SAVE);
            }
        }
    }

}
