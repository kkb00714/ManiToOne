package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.MatchProcessStatus;
import com.finalproject.manitoone.dto.manito.ManitoMatchResponseDto;
import com.finalproject.manitoone.repository.MatchProcessStatusRepository;
import com.finalproject.manitoone.service.AsyncMatchService;
import com.finalproject.manitoone.service.ManitoMatchesService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@RestController
@RequestMapping("/api/manito")
@RequiredArgsConstructor
public class ManitoMatchController {
  private final AsyncMatchService asyncMatchService;
  private final ManitoMatchesService manitoMatchesService;
  private final MatchProcessStatusRepository statusRepository;

  private String validateSession(HttpSession session) {
    String nickname = (String) session.getAttribute("nickname");
    if (nickname == null) {
      throw new AccessDeniedException("로그인이 필요합니다.");
    }
    return nickname;
  }

  // 비동기 매칭 요청
  @PostMapping("/match/request")
  public ResponseEntity<?> requestMatch(@SessionAttribute("nickname") String nickname) {
    try {
      // 진행 중인 매칭 프로세스 확인
      boolean hasInProgressMatch = statusRepository.existsByNicknameAndStatusAndTimeoutAtAfter(
          nickname,
          MatchProcessStatus.ProcessStatus.IN_PROGRESS,
          LocalDateTime.now()
      );

      if (hasInProgressMatch) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", "매칭 프로세스가 진행 중입니다."));
      }

      // 비동기 매칭 프로세스 시작
      asyncMatchService.processMatchAsync(nickname);
      return ResponseEntity.accepted()
          .body(Map.of("message", "매칭이 시작되었습니다."));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping("/match/status")
  public ResponseEntity<?> getMatchStatus(@SessionAttribute("nickname") String nickname) {
    Optional<MatchProcessStatus> status = statusRepository
        .findFirstByNicknameAndStatusOrderByCreatedAtDesc(
            nickname,
            MatchProcessStatus.ProcessStatus.IN_PROGRESS
        );

    if (status.isEmpty()) {
      // 진행 중인 프로세스가 없는 경우, 완료된 프로세스가 있는지 확인
      Optional<MatchProcessStatus> completedStatus = statusRepository
          .findFirstByNicknameAndStatusOrderByCreatedAtDesc(
              nickname,
              MatchProcessStatus.ProcessStatus.COMPLETED
          );

      if (completedStatus.isPresent()) {
        // 완료된 프로세스가 있다면 리로드
        return ResponseEntity.ok()
            .body(Map.of(
                "status", "COMPLETED",
                "message", "매칭이 완료되었습니다.",
                "shouldReload", true
            ));
      }

      return ResponseEntity.ok()
          .body(Map.of(
              "status", "NO_PROCESS",
              "message", "진행 중인 매칭 프로세스가 없습니다.",
              "shouldReload", false
          ));
    }

    MatchProcessStatus processStatus = status.get();
    if (processStatus.isTimedOut()) {
      processStatus.fail();
      statusRepository.save(processStatus);
      return ResponseEntity.ok()
          .body(Map.of(
              "status", "FAILED",
              "message", "매칭 프로세스가 시간 초과되었습니다.",
              "shouldReload", false
          ));
    }

    return ResponseEntity.ok()
        .body(Map.of(
            "status", "IN_PROGRESS",
            "message", "매칭 프로세스가 진행 중입니다.",
            "shouldReload", false
        ));
  }

  // Pass 처리
  @PutMapping("/pass/{manitoMatchesId}")
  public ResponseEntity<Void> passManitoMatch(
      @PathVariable Long manitoMatchesId,
      HttpSession session
  ) {
    String nickname = validateSession(session);
    manitoMatchesService.passMatch(manitoMatchesId, nickname);
    return ResponseEntity.ok().build();
  }

  @Deprecated
  @PostMapping
  public ResponseEntity<ManitoMatchResponseDto> createMatch(HttpSession session) {
    String nickname = validateSession(session);
    ManitoMatches match = manitoMatchesService.createMatch(nickname);
    return ResponseEntity.ok(ManitoMatchResponseDto.from(match));
  }
}
