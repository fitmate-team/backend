package com.fitmate.backend.member.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExerciseLevel {
    FIRST_TIME(0),
    BEGINNER(1),
    INTERMEDIATE(2),
    ADVANCED(3);

    private final int level;

}
