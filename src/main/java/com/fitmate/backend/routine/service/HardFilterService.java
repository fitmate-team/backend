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

    // =========================================================
    // 1. 필요한 Repository 주입
    // =========================================================

    // [1] 전체 Exercise Master 조회용 Repository
    //     - Hard Filter의 시작점인 전체 운동 목록 조회 목적
    private final ExerciseRepository exerciseRepository;

    // [2] 이번 루틴 생성에 사용할 WorkoutEnvironment 조회용 Repository
    //     - 현재 운동 장소(locationType) 확인 목적
    //     - 현재 환경의 보유 기구(equipment) 확인 목적
    //     - environmentId가 실제 해당 회원 소유인지 함께 확인 필요
    private final WorkoutEnvironmentRepository workoutEnvironmentRepository;

    // [3] MemberProfile 조회용 Repository
    //     - 사용자의 운동 경험 수준(exerciseLevel) 확인 목적
    //     - 사용자의 회피 부위(avoidBodyAreas) 확인 목적
    private final MemberProfileRepository memberProfileRepository;

    // [4] Member 조회용 Repository
    //     - 사용자가 직접 제외한 운동(excludedExercises) 확인 목적
    private final MemberRepository memberRepository;

    // [5] WorkoutExerciseRecord 조회용 Repository
    //     - 전주에 실제 수행한 운동 확인 목적
    //     - 단순 루틴 포함 여부가 아니라 실제 수행 여부 확인 필요
    private final WorkoutExer


    // =========================================================
    // 2. Hard Filter 메인 메서드
    // =========================================================

    // [6] AI에게 전달 가능한 운동 목록을 만드는 public 메서드 선언
    //
    //     필요한 입력값 고민
    //     - 어떤 회원인지 식별할 memberId
    //     - 이번 루틴 생성에 사용할 운동환경을 식별할 environmentId
    //     - 전주 범위를 계산하려면 이번 주의 시작일이 필요한지 고민
    //
    //     반환값
    //     - Hard Filter를 모두 통과한 List<Exercise>



    // =========================================================
    // 3. 필터링에 필요한 데이터 준비
    // =========================================================

    // [7] environmentId와 memberId를 이용해 WorkoutEnvironment 조회
    //
    //     단순 findById가 아니라
    //     "이 운동환경이 해당 회원의 것인가?"까지 확인할 수 있는 조회 방식 고려
    //
    //     조회 실패 시
    //     프로젝트의 CustomException / ErrorCode 사용


    // [8] memberId를 이용해 MemberProfile 조회
    //
    //     이후 사용할 값
    //     - exerciseLevel
    //     - avoidBodyAreas
    //
    //     조회 실패 시 어떤 ErrorCode를 사용할지 기존 코드 확인


    // [9] memberId를 이용해 Member 조회
    //
    //     이후 사용할 값
    //     - excludedExercises


    // [10] ExerciseRepository에서 전체 운동 목록 조회
    //
    //      이 목록이 Hard Filter의 최초 후보군


    // [11] 전주의 시작일과 종료일 계산
    //
    //      예시 개념
    //      이번 주 시작일이 월요일이라면
    //      전주 시작일 = 이번 주 시작일 - 7일
    //      전주 종료일 = 이번 주 시작일 - 1일
    //
    //      LocalDate 연산 방법 확인


    // [12] WorkoutExerciseRecordRepository를 이용해
    //      해당 회원의 "전주 실제 수행 기록" 조회
    //
    //      필요한 조건 고민
    //      - 해당 member의 기록인지
    //      - DailyRoutine.routineDate가 전주 범위인지
    //      - 실제 수행한 기록인지
    //
    //      실제 수행 여부 판단 후보
    //      - completedSetCount > 0
    //      - completedDurationSeconds > 0
    //
    //      둘 중 하나라도 0보다 크면 실제 수행으로 볼 수 있는지 고민


    // [13] 전주 실제 수행 기록에서
    //      "이번 주에 제외해야 하는 Exercise"만 뽑아서 별도 집합으로 준비
    //
    //      주의
    //      - CARDIO는 지난주에 했어도 다시 추천 가능
    //      - non-CARDIO만 제외 대상
    //
    //      List보다 Set으로 만들어두면
    //      이후 포함 여부 검사에 어떤 장점이 있는지도 생각



    // =========================================================
    // 4. 전체 Exercise를 하나씩 검사
    // =========================================================

    // [14] 전체 Exercise 목록을 순회하면서
    //      아래 조건을 모두 통과한 운동만 최종 목록에 포함
    //
    //      한 조건이라도 실패하면 해당 Exercise 탈락



    // =========================================================
    // 5. 장소 조건
    // =========================================================

    // [15] 현재 WorkoutEnvironment의 locationType 확인


    // [16] 현재 Exercise의 availableLocations 확인


    // [17] 현재 locationType이
    //      Exercise.availableLocations 안에 포함되어 있는지 검사
    //
    //      포함됨 -> 다음 조건으로 진행
    //      포함되지 않음 -> 해당 Exercise 탈락
    //
    //      Set에서 특정 값 포함 여부를 확인하는 메서드 생각



    // =========================================================
    // 6. 기구 조건
    // =========================================================

    // [18] 현재 Exercise가 요구하는 equipment 확인


    // [19] 현재 WorkoutEnvironment가 보유한 equipment 확인


    // [20] Exercise가 요구하는 모든 기구를
    //      WorkoutEnvironment가 가지고 있는지 검사
    //
    //      예시
    //      운동 필요 기구 = [덤벨, 벤치]
    //      환경 보유 기구 = [덤벨]
    //      -> 탈락
    //
    //      운동 필요 기구 = [덤벨, 벤치]
    //      환경 보유 기구 = [덤벨, 벤치, 밴드]
    //      -> 통과
    //
    //      "하나라도 포함"이 아니라 "전부 포함"이라는 점 주의
    //
    //      Collection에서 다른 Collection의 모든 요소를 포함하는지
    //      확인하는 방법 탐색



    // =========================================================
    // 7. 운동 경험 수준 조건
    // =========================================================

    // [21] 현재 Exercise의 minimumExperienceLevel 확인


    // [22] MemberProfile의 exerciseLevel 확인


    // [23] 사용자 운동 레벨이
    //      해당 운동의 최소 요구 레벨 이상인지 검사
    //
    //      예시
    //      사용자 = BEGINNER
    //      운동 최소 수준 = INTERMEDIATE
    //      -> 탈락
    //
    //      사용자 = INTERMEDIATE
    //      운동 최소 수준 = BEGINNER
    //      -> 통과
    //
    //      enum 값의 순서를 어떻게 비교할지 고민
    //
    //      단순 문자열 비교는 부적절
    //      enum 선언 순서를 이용할지
    //      별도 비교 메서드를 둘지 고민



    // =========================================================
    // 8. 회피 부위 조건
    // =========================================================

    // [24] 현재 Exercise의 relatedBodyAreas 확인


    // [25] MemberProfile의 avoidBodyAreas 확인


    // [26] 두 Set에 하나라도 같은 BodyArea가 존재하는지 검사
    //
    //      하나라도 겹침 -> 탈락
    //      하나도 겹치지 않음 -> 통과
    //
    //      여기서는 "모두 포함"이 아니라
    //      "교집합이 존재하는가?"를 판단하는 문제



    // =========================================================
    // 9. 사용자가 직접 제외한 운동 조건
    // =========================================================

    // [27] Member의 excludedExercises 확인


    // [28] 현재 Exercise가 excludedExercises 안에 있는지 검사
    //
    //      있음 -> 탈락
    //      없음 -> 통과



    // =========================================================
    // 10. 전주 실제 수행 운동 조건
    // =========================================================

    // [29] 앞에서 준비한
    //      "전주 실제 수행 non-CARDIO 운동 집합" 안에
    //      현재 Exercise가 존재하는지 검사
    //
    //      존재함 -> 탈락
    //      존재하지 않음 -> 통과
    //
    //      CARDIO는 애초에 제외 집합을 만들 때 넣지 않는 방식 고려



    // =========================================================
    // 11. 최종 반환
    // =========================================================

    // [30] 위 모든 조건을 통과한 Exercise만 모아서
    //      List<Exercise> 형태로 반환
    //
    //      이 반환값이 이후 단계에서
    //      ELIGIBLE_EXERCISES DTO로 변환될 후보군



    // =========================================================
    // 12. 리팩터링 후보 - 지금 바로 하지 않아도 되는 부분
    // =========================================================

    // 처음에는 메인 메서드 안에서 조건을 하나씩 구현해도 무방
    //
    // 코드가 길어진 뒤 아래처럼 의미별 메서드 분리 고려
    //
    // 장소 통과 여부 판단 메서드
    // 기구 통과 여부 판단 메서드
    // 경험 수준 통과 여부 판단 메서드
    // 회피 부위 통과 여부 판단 메서드
    // 제외 운동 통과 여부 판단 메서드
    // 전주 운동 통과 여부 판단 메서드
    //
    // 지금 단계에서는 처음부터 과하게 쪼개기보다
    // 실제 로직을 한 번 구현하고 반복/복잡성이 보일 때 분리 추천
}
