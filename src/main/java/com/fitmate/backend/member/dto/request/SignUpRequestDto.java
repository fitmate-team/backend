package com.fitmate.backend.member.dto.request;

import com.fitmate.backend.equipment.domain.Equipment;
import com.fitmate.backend.member.domain.*;
import com.fitmate.backend.member.domain.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Schema(description = "회원가입 요청 DTO")
@NoArgsConstructor // 기본 생성자
@Getter
public class SignUpRequestDto {

    // Member

    @Schema(description = "로그인 아이디 (8~16자)", example = "asdf1234")
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 8, max = 16, message = "아이디는 8~16자여야 합니다.")
    private String loginId;

    @Schema(description = "비밀번호 (영문, 숫자 포함 10자 이상)", example = "asdfasdf1234")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{10,}$", message = "비밀번호는 영문과 숫자를 포함한 10자리 이상이어야 " +
            "합니다.")
    private String password;


    // MemberProfile

    @Schema(description = "성별", example = "FEMALE")
    @NotNull(message = "성별을 선택해주세요.")
    private Gender gender;

    @Schema(description = "나이", example = "25")
    @NotNull(message = "나이를 입력해주세요.")
    @Positive(message = "나이는 양수여야 합니다.")
    private Integer age;

    @Schema(description = "키(cm)", example = "165.5")
    @NotNull(message = "키를 입력해주세요.")
    @Positive(message = "키는 양수여야 합니다.")
    private Double height;

    @Schema(description = "운동 경험 수준", example = "BEGINNER")
    @NotNull(message = "운동 경험 수준을 선택해주세요.")
    private ExerciseLevel exerciseLevel;

    @Schema(description = "현재 운동 상태", example = "STARTING")
    @NotNull(message = "현재 운동 상태를 선택해주세요.")
    private CurrentExerciseStatus currentExerciseStatus;

    @Schema(description = "주 운동 목표", example = "MUSCLE_GAIN")
    @NotNull(message = "운동 목표를 선택해주세요.")
    private PrimaryGoal primaryGoal;

    @Schema(description = "운동 목표 세부 전략", example = "MUSCLE_GAIN_HYPERTROPHY_FOCUS")
    @NotNull(message = "운동 목표 전략을 선택해주세요.")
    private GoalStrategy goalStrategy;

    @Schema(description = "주간 운동 횟수", example = "3")
    @NotNull(message = "주간 운동 횟수를 선택해주세요.")
    @Min(value = 1, message = "주간 운동 횟수는 최소 1회여야 합니다.")
    @Max(value = 7, message = "주간 운동 횟수는 최대 7회입니다.")
    private Integer weeklyFrequency;

    @Schema(description = "1회 운동 가능 시간(분)", example = "60")
    @NotNull(message = "운동 시간을 선택해주세요.")
    @Positive(message = "운동 시간은 양수여야 합니다.")
    private Integer sessionMinutes;

    @Schema(description = "운동 장소", example = "GYM")
    @NotNull(message = "운동 장소를 선택해주세요.")
    private ExerciseLocation exerciseLocation;

    @Schema(description = "헬스장 이름 (운동 장소가 GYM인 경우 사용)", example = "Fit Gym")
    private String gymName;

    @Schema(description = "헬스장 주소 (운동 장소가 GYM인 경우 사용)", example = "서울특별시 노원구 동일로 123")
    private String gymAddress;

    @Schema(description = "선택한 운동기구 코드 목록", example = "[\"EQ_DUMBBELL\", \"EQ_BARBELL\"]")
    @NotNull(message = "운동기구 목록은 null일 수 없습니다.")
    private Set<String> equipmentCodes = new HashSet<>();

    @Schema(description = "운동 가능한 요일", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]")
    @NotEmpty(message = "운동 가능한 요일을 하나 이상 선택해주세요.")
    private Set<DayOfWeek> availableDays;

    @Schema(description = "운동 시 피하고 싶은 신체 부위", example = "[\"KNEE\", \"LOWER_BACK\"]")
    private Set<BodyArea> avoidBodyAreas = new HashSet<>();

    @Schema(description = "골격근량(kg), 선택값", example = "23.5")
    @Positive(message = "골격근량은 양수여야 합니다.")
    private Double skeletalMuscleMass;

    @Schema(description = "체지방률(%), 선택값", example = "22.3")
    @Positive(message = "체지방률은 양수여야 합니다.")
    private Double bodyFatPercentage;

    @Schema(description = "체지방량(kg), 선택값", example = "12.4")
    @Positive(message = "체지방량은 양수여야 합니다.")
    private Double bodyFatMass;


    // BodyWeight

    @Schema(description = "몸무게(kg)", example = "55.4")
    @NotNull(message = "몸무게를 입력해주세요.")
    @Positive(message = "몸무게는 양수여야 합니다.")
    private Double weight;

    public Member toMember(String encodedPassword) {
        return Member.builder().loginId(this.loginId).password(encodedPassword).build();
    }

    public MemberProfile toMemberProfile(Member member) {
        return MemberProfile.builder()
                .member(member)
                .gender(this.gender)
                .age(this.age)
                .height(this.height)
                .exerciseLevel(this.exerciseLevel)
                .currentExerciseStatus(this.currentExerciseStatus)
                .primaryGoal(this.primaryGoal)
                .goalStrategy(this.goalStrategy)
                .weeklyFrequency(this.weeklyFrequency)
                .sessionMinutes(this.sessionMinutes)
                .exerciseLocation(this.exerciseLocation)
                .availableDays(this.availableDays)
                .avoidBodyAreas(this.avoidBodyAreas)
                .skeletalMuscleMass(this.skeletalMuscleMass)
                .bodyFatPercentage(this.bodyFatPercentage)
                .bodyFatMass(this.bodyFatMass)
                .build();
    }

    public BodyWeight toBodyWeight(Member member) {
        return BodyWeight.builder().member(member).weight(this.weight).build();
    }

    public WorkoutEnvironment toWorkoutEnvironment(Member member,
                                                   boolean isDefault,
                                                   Set<Equipment> equipment) {
        return WorkoutEnvironment.builder()
                .member(member)
                .gymName(this.gymName)
                .gymAddress(this.gymAddress)
                .defaultGym(isDefault)
                .locationType(this.exerciseLocation)
                .equipment(equipment)
                .build();
    }
}
