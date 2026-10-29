package com.fitmate.backend.routine.ai.service;

import com.fitmate.backend.routine.ai.dto.common.ExerciseRoutineOutputDto;
import com.fitmate.backend.routine.ai.dto.weekly.WeeklyRoutineDayOutputDto;
import com.fitmate.backend.routine.ai.dto.weekly.WeeklyRoutineOutputDto;
import com.fitmate.backend.routine.domain.ExerciseRoutineAdjustment;
import com.fitmate.backend.routine.repository.ExerciseRoutineAdjustmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyAdjustmentApplyService {

    private final ExerciseRoutineAdjustmentRepository adjustmentRepository;

    // 반복 운동 1회당 예상 소요 시간
    private static final int SECONDS_PER_REP = 3;

    public void applyPendingAdjustments(Long memberId,
                                        WeeklyRoutineOutputDto output,
                                        Integer sessionMinutes) {
        // 1. 회원의 대기 조정값을 운동 코드 기준으로 정리
        Map<String, ExerciseRoutineAdjustment> adjustments =
                adjustmentRepository.findAllByMember_Id(
                        memberId)
                .stream()
                .collect(Collectors.toMap(a -> a.getExercise().getExerciseCode(), a -> a));

        // 2. 각 운동일의 AI 원본 예상 시간
        Map<WeeklyRoutineDayOutputDto, Long> daySeconds = new HashMap<>();

        // 3. 동일 운동 코드의 모든 등장 위치를 묶기
        Map<String, List<ExercisePosition>> positions = new LinkedHashMap<>();

        for (WeeklyRoutineDayOutputDto day : output.getWeeklyPlan()) {

            daySeconds.put(day, day.getEstimatedMinutes() * 60L);

            for (ExerciseRoutineOutputDto exercise : day.getExercises()) {
                positions.computeIfAbsent(exercise.getExerciseCode(), key -> new ArrayList<>())
                        .add(new ExercisePosition(day, exercise));
            }
        }

        long limitSeconds = sessionMinutes * 60L;

        // 4. 운동 코드 단위로 조정 적용 시도
        for (Map.Entry<String, List<ExercisePosition>> entry : positions.entrySet()) {

            ExerciseRoutineAdjustment adjustment = adjustments.get(entry.getKey());

            if (adjustment == null) {
                continue;
            }

            List<ExercisePosition> targets = entry.getValue();

            // 5. 조정 전후 예상 시간 차이 계산
            Map<WeeklyRoutineDayOutputDto, Long> changes = new HashMap<>();

            for (ExercisePosition target : targets) {

                ExerciseRoutineOutputDto original = target.exercise();

                long before = estimateSeconds(original.getSets(),
                                              original.getReps(),
                                              original.getDurationSeconds(),
                                              original.getRestSeconds());

                long after = estimateSeconds(adjustment.getSetCount(),
                                             adjustment.getReps(),
                                             adjustment.getDurationSeconds(),
                                             original.getRestSeconds());

                changes.merge(target.day(), after - before, Long::sum);
            }

            // 6. 해당 운동 코드를 적용했을 때
            //    하루라도 운동 가능 시간을 초과하는지 확인
            boolean exceedsTime = changes.entrySet()
                    .stream()
                    .anyMatch(change -> daySeconds.get(change.getKey()) + change.getValue() >
                            limitSeconds);

            if (exceedsTime) {
                // 모든 날짜에서 원본 처방 유지
                // 대기 조정값도 DB에 그대로 보관
                continue;
            }

            // 7. 모든 날짜에서 적용 가능할 때만 일괄 적용
            for (ExercisePosition target : targets) {

                target.exercise()
                        .applyAdjustment(adjustment.getId(),
                                         adjustment.getWeight(),
                                         adjustment.getReps(),
                                         adjustment.getSetCount(),
                                         adjustment.getDurationSeconds());
            }

            // 8. 적용에 성공한 시간 변화만 반영
            changes.forEach((day, delta) -> daySeconds.put(day, daySeconds.get(day) + delta));
        }

        // 9. 모든 조정이 끝난 뒤 예상 시간 갱신
        for (WeeklyRoutineDayOutputDto day : output.getWeeklyPlan()) {

            long seconds = Math.max(60L, daySeconds.get(day));
            int minutes = (int) ((seconds + 59) / 60);

            day.updateEstimatedMinutes(minutes);
        }
    }

    // 반복 운동과 시간 기반 운동의 예상 수행 시간 계산
    private long estimateSeconds(Integer sets,
                                 Integer reps,
                                 Integer durationSeconds,
                                 Integer restSeconds) {
        // DURATION은 운동 전체 수행 시간
        if (durationSeconds != null) {
            return durationSeconds;
        }

        long exerciseSeconds = (long) sets * reps * SECONDS_PER_REP;

        long restTime = (long) (sets - 1) * restSeconds;

        return exerciseSeconds + restTime;
    }

    // 어떤 날짜의 어떤 운동인지 보관
    private record ExercisePosition(WeeklyRoutineDayOutputDto day,
                                    ExerciseRoutineOutputDto exercise) {
    }
}
