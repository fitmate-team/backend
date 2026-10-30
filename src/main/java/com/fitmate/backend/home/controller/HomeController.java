package com.fitmate.backend.home.controller;

import com.fitmate.backend.home.dto.response.HomeResponseDto;
import com.fitmate.backend.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 조회 API")
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 로그인한 회원의 홈 화면 정보를 조회합니다.
     */
    @Operation(
            summary = "홈 화면 조회",
            description = """
                    이번 주 루틴 존재 여부, 운동 완료 횟수, 목표 횟수,
                    연속 운동 횟수와 오늘의 운동 요약을 조회합니다.
                    연속 운동 횟수는 추천된 운동일의 완료 여부를 기준으로 계산하며,
                    오늘 운동이 미완료인 경우 기존 연속 횟수를 유지합니다.
                    이번 주 루틴이나 오늘 예정된 운동이 없어도 정상 응답합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<HomeResponseDto> getHome(
            @AuthenticationPrincipal Long memberId
    ) {
        // Access Token으로 인증된 회원의 홈 화면 정보를 조회합니다.
        HomeResponseDto responseDto = homeService.getHome(memberId);

        return ResponseEntity.ok(responseDto);
    }
}