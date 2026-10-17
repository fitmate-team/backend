package com.fitmate.backend.member.service;

import com.fitmate.backend.global.exception.CustomException;
import com.fitmate.backend.global.exception.ErrorCode;
import com.fitmate.backend.member.domain.BodyComposition;
import com.fitmate.backend.member.domain.Member;
import com.fitmate.backend.member.dto.request.BodyCompositionCreateRequestDto;
import com.fitmate.backend.member.repository.BodyCompositionRepository;
import com.fitmate.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BodyCompositionService {

    private final MemberRepository memberRepository;
    private final BodyCompositionRepository bodyCompositionRepository;

    @Transactional
    public void createBodyComposition(
            Long memberId,
            BodyCompositionCreateRequestDto requestDto
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        boolean alreadyExists =
                bodyCompositionRepository
                        .existsByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                memberId,
                                startOfDay,
                                startOfNextDay
                        );

        if (alreadyExists) {
            throw new CustomException(ErrorCode.DUPLICATE_BODY_COMPOSITION);
        }

        BodyComposition bodyComposition = BodyComposition.of(
                member,
                requestDto.getWeight(),
                requestDto.getSkeletalMuscleMass(),
                requestDto.getBodyFatPercentage(),
                requestDto.getBodyFatMass()
        );

        bodyCompositionRepository.save(bodyComposition);
    }
}