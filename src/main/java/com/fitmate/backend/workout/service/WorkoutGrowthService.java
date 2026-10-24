package com.fitmate.backend.workout.service;

import com.fitmate.backend.exercise.domain.ExerciseBaselineType;
import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.BodyComposition;
import com.fitmate.backend.member.repository.BodyCompositionRepository;
import com.fitmate.backend.member.repository.MemberRepository;
import com.fitmate.backend.workout.dto.response.ExerciseGrowthResponseDto;
import com.fitmate.backend.workout.dto.response.GrowthAnalysisResponseDto;
import com.fitmate.backend.workout.dto.response.GrowthAnalysisResponseDto.*;
import com.fitmate.backend.workout.repository.WorkoutGrowthRepository;
import com.fitmate.backend.workout.repository.WorkoutGrowthRepository.*;
import com.fitmate.backend.workout.dto.response.ExerciseGrowthResponseDto.WeightMonthlyRecordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutGrowthService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final MemberRepository memberRepository;
    private final BodyCompositionRepository bodyCompositionRepository;
    private final WorkoutGrowthRepository workoutGrowthRepository;

    public GrowthAnalysisResponseDto getGrowthAnalysis(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now(KOREA_ZONE);
        LocalDate today = now.toLocalDate();

        YearMonth currentMonth = YearMonth.from(today);
        YearMonth firstMonth = currentMonth.minusMonths(5);

        List<YearMonth> months = IntStream.range(0, 6)
                .mapToObj(firstMonth::plusMonths)
                .toList();

        LocalDateTime startAt = firstMonth.atDay(1).atStartOfDay();

        return GrowthAnalysisResponseDto.of(
                getBodyComposition(memberId, months, startAt, now),
                getWorkoutGrowth(memberId, months, startAt, now),
                getAttendance(memberId, months, today, now)
        );
    }

    public ExerciseGrowthResponseDto getExerciseGrowth(
            Long memberId,
            Long exerciseId
    ) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now(KOREA_ZONE);

        YearMonth firstMonth = YearMonth.from(now).minusMonths(5);

        List<YearMonth> months = IntStream.range(0, 6)
                .mapToObj(firstMonth::plusMonths)
                .toList();

        LocalDateTime startAt = firstMonth.atDay(1).atStartOfDay();

        ExerciseProjection exercise = workoutGrowthRepository
                .findPerformedWeightExercises(
                        memberId,
                        startAt,
                        now,
                        ExerciseBaselineType.WEIGHT_REPS_SETS
                )
                .stream()
                .filter(item -> item.getExerciseId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.EXERCISE_GROWTH_RECORD_NOT_FOUND
                        )
                );

        return getExerciseGrowth(
                memberId,
                ExerciseDto.of(
                        exercise.getExerciseId(),
                        exercise.getExerciseName()
                ),
                months,
                startAt,
                now
        );
    }

    private BodyCompositionDto getBodyComposition(
            Long memberId,
            List<YearMonth> months,
            LocalDateTime startAt,
            LocalDateTime now
    ) {
        Map<YearMonth, BodyMonthlyProjection> recordsByMonth =
                workoutGrowthRepository
                        .findBodyMonthlyRecords(memberId, startAt, now)
                        .stream()
                        .collect(Collectors.toMap(
                                record -> YearMonth.of(
                                        record.getYear(),
                                        record.getMonth()
                                ),
                                Function.identity()
                        ));

        List<BodyMonthlyRecordDto> monthlyRecords = new ArrayList<>();

        for (YearMonth month : months) {
            BodyMonthlyProjection record = recordsByMonth.get(month);

            monthlyRecords.add(BodyMonthlyRecordDto.of(
                    month.getYear(),
                    month.getMonthValue(),
                    RangeDto.of(
                            record == null ? null : record.getMaxWeight(),
                            record == null ? null : record.getMinWeight()
                    ),
                    RangeDto.of(
                            record == null
                                    ? null
                                    : record.getMaxSkeletalMuscleMass(),
                            record == null
                                    ? null
                                    : record.getMinSkeletalMuscleMass()
                    ),
                    RangeDto.of(
                            record == null
                                    ? null
                                    : record.getMaxBodyFatPercentage(),
                            record == null
                                    ? null
                                    : record.getMinBodyFatPercentage()
                    ),
                    RangeDto.of(
                            record == null ? null : record.getMaxBodyFatMass(),
                            record == null ? null : record.getMinBodyFatMass()
                    )
            ));
        }

        return BodyCompositionDto.of(
                getLatestBodyComposition(memberId, now),
                monthlyRecords
        );
    }

    private LatestBodyCompositionDto getLatestBodyComposition(
            Long memberId,
            LocalDateTime now
    ) {
        Optional<BodyComposition> weight = bodyCompositionRepository
                .findTopByMember_IdAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(
                        memberId,
                        now
                );

        Optional<BodyComposition> muscle = bodyCompositionRepository
                .findTopByMember_IdAndSkeletalMuscleMassIsNotNullAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(
                        memberId,
                        now
                );

        Optional<BodyComposition> fatPercentage = bodyCompositionRepository
                .findTopByMember_IdAndBodyFatPercentageIsNotNullAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(
                        memberId,
                        now
                );

        Optional<BodyComposition> fatMass = bodyCompositionRepository
                .findTopByMember_IdAndBodyFatMassIsNotNullAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(
                        memberId,
                        now
                );

        return LatestBodyCompositionDto.of(
                toMeasurement(weight, BodyComposition::getWeight),
                toMeasurement(muscle, BodyComposition::getSkeletalMuscleMass),
                toMeasurement(fatPercentage, BodyComposition::getBodyFatPercentage),
                toMeasurement(fatMass, BodyComposition::getBodyFatMass)
        );
    }

    private MeasurementDto toMeasurement(
            Optional<BodyComposition> record,
            Function<BodyComposition, Double> valueExtractor
    ) {
        return record
                .map(body -> MeasurementDto.of(
                        valueExtractor.apply(body),
                        body.getCreatedAt().toLocalDate()
                ))
                .orElseGet(() -> MeasurementDto.of(null, null));
    }

    private WorkoutGrowthDto getWorkoutGrowth(
            Long memberId,
            List<YearMonth> months,
            LocalDateTime startAt,
            LocalDateTime now
    ) {
        List<ExerciseProjection> performedExercises =
                workoutGrowthRepository.findPerformedWeightExercises(
                        memberId,
                        startAt,
                        now,
                        ExerciseBaselineType.WEIGHT_REPS_SETS
                );

        List<ExerciseDto> exercises = performedExercises.stream()
                .map(exercise -> ExerciseDto.of(
                        exercise.getExerciseId(),
                        exercise.getExerciseName()
                ))
                .toList();

        if (exercises.isEmpty()) {
            return WorkoutGrowthDto.of(exercises, null);
        }

        ExerciseDto defaultExercise = exercises.get(0);

        ExerciseGrowthResponseDto selectedExercise = getExerciseGrowth(
                memberId,
                defaultExercise,
                months,
                startAt,
                now
        );

        return WorkoutGrowthDto.of(
                exercises,
                selectedExercise
        );
    }

    private ExerciseGrowthResponseDto getExerciseGrowth(
            Long memberId,
            ExerciseDto exercise,
            List<YearMonth> months,
            LocalDateTime startAt,
            LocalDateTime now
    ) {
        Map<YearMonth, WeightMonthlyProjection> recordsByMonth =
                workoutGrowthRepository.findWeightMonthlyRecords(
                                memberId,
                                exercise.getExerciseId(),
                                startAt,
                                now,
                                ExerciseBaselineType.WEIGHT_REPS_SETS
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                record -> YearMonth.of(
                                        record.getYear(),
                                        record.getMonth()
                                ),
                                Function.identity()
                        ));

        List<WeightMonthlyRecordDto> monthlyRecords = new ArrayList<>();

        for (YearMonth month : months) {
            WeightMonthlyProjection record = recordsByMonth.get(month);

            monthlyRecords.add(WeightMonthlyRecordDto.of(
                    month.getYear(),
                    month.getMonthValue(),
                    record == null ? null : record.getMaxWeight(),
                    record == null ? null : record.getMinWeight()
            ));
        }

        return ExerciseGrowthResponseDto.of(
                exercise.getExerciseId(),
                exercise.getExerciseName(),
                monthlyRecords
        );
    }

    private AttendanceDto getAttendance(
            Long memberId,
            List<YearMonth> months,
            LocalDate today,
            LocalDateTime now
    ) {
        Map<YearMonth, AttendanceMonthlyProjection> recordsByMonth =
                workoutGrowthRepository.findAttendanceMonthlyRecords(
                                memberId,
                                months.get(0).atDay(1),
                                today,
                                now
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                record -> YearMonth.of(
                                        record.getYear(),
                                        record.getMonth()
                                ),
                                Function.identity()
                        ));

        List<AttendanceMonthlyRecordDto> monthlyRecords = new ArrayList<>();

        for (YearMonth month : months) {
            AttendanceMonthlyProjection record = recordsByMonth.get(month);

            Integer attendanceRate = null;

            if (record != null && record.getScheduledCount() > 0) {
                attendanceRate = (int) Math.round(
                        record.getAttendedCount() * 100.0
                                / record.getScheduledCount()
                );
            }

            monthlyRecords.add(AttendanceMonthlyRecordDto.of(
                    month.getYear(),
                    month.getMonthValue(),
                    attendanceRate
            ));
        }

        return AttendanceDto.of(monthlyRecords);
    }
}