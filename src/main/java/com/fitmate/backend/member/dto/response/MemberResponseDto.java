package com.fitmate.backend.member.dto.response;

import com.fitmate.backend.member.domain.ExerciseGoal;
import com.fitmate.backend.member.domain.ExerciseLevel;
import com.fitmate.backend.member.domain.Gender;
import com.fitmate.backend.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponseDto {
    private Long id;
    private String loginId;
    private String nickname;
    private Gender gender;
    private Double height;
    private Double weight;
    private ExerciseLevel exerciseLevel;
    private ExerciseGoal exerciseGoal;
    private Double targetWeight;


    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.getGender(),
                member.getHeight(),
                member.getWeight(),
                member.getExerciseLevel(),
                member.getExerciseGoal(),
                member.getTargetWeight()
        );

    }
}
