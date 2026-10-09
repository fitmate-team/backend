package com.fitmate.backend.routine.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MainTarget {

    CHEST("상체 PUSH"),
    BACK("상체 PULL"),
    SHOULDERS("어깨"),
    LEGS_GLUTES("하체"),
    ARMS("팔"),
    UPPER_BODY("상체"),
    FULL_BODY("전신"),
    CARDIO("유산소");

    private final String displayTitle;
}