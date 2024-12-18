package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.MatchProcessStatus;
import com.finalproject.manitoone.dto.manito.ManitoLetterResponseDto;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.repository.MatchProcessStatusRepository;
import com.finalproject.manitoone.service.AsyncMatchService;
import com.finalproject.manitoone.service.ManitoMatchesService;
import com.finalproject.manitoone.service.ManitoService;
import com.finalproject.manitoone.service.PostService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/manito")
@RequiredArgsConstructor
public class ManitoViewController {

  private final ManitoService manitoService;
  private final PostService postService;
  private final ManitoMatchesService manitoMatchesService;
  private final AsyncMatchService asyncMatchService;
  private final MatchProcessStatusRepository statusRepository;

  @GetMapping("/fragments/manito-letter")
  public String getManitoLetterFragment(@RequestParam Long letterId, Model model) {
    try {
      ManitoLetterResponseDto letter = manitoService.getLetter(letterId);
      model.addAttribute("letter", letter);
      return "fragments/common/manito-letter :: manito-letter(letter=${letter})";
    } catch (Exception e) {
      log.error("Error loading letter fragment: ", e);
      return "error/fragment";
    }
  }

  @GetMapping
  public String getManitoPage(
      @RequestParam(required = false) String tab,
      @RequestParam(required = false) String letterId,
      HttpSession session,
      Model model
  ) {
    String nickname = (String) session.getAttribute("nickname");
    if (nickname == null) {
      return "redirect:/login";
    }

    try {
      model.addAttribute("userNickname", nickname);
      model.addAttribute("initialTab", tab);
      model.addAttribute("initialLetterId", letterId);

      // 현재 유효한 매칭 확인
      Optional<ManitoMatches> currentMatch = manitoMatchesService.getCurrentValidMatch(nickname);

      if (currentMatch.isPresent()) {
        // 현재 유효한 매칭이 있는 경우 처리
        handleExistingMatch(currentMatch.get(), model);
      } else {
        // 진행 중인 매칭 프로세스 확인
        Optional<MatchProcessStatus> inProgressStatus = statusRepository
            .findFirstByNicknameAndStatusOrderByCreatedAtDesc(
                nickname,
                MatchProcessStatus.ProcessStatus.IN_PROGRESS
            );

        if (inProgressStatus.isPresent()) {
          if (inProgressStatus.get().isTimedOut()) {
            // 타임아웃된 프로세스 처리
            handleTimedOutProcess(inProgressStatus.get(), model);
          } else {
            // 진행 중인 프로세스 처리
            handleInProgressMatch(nickname, model);
          }
        } else {
          // 새로운 매칭 가능 여부 확인
          checkMatchAvailability(nickname, model);
        }
      }

      return "pages/manito";

    } catch (Exception e) {
      log.error("Error in getManitoPage: ", e);
      model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다.");
      return "pages/manito";
    }
  }

  // 기존 매칭 처리
  private void handleExistingMatch(ManitoMatches match, Model model) {
    try {
      PostViewResponseDto todaysPost = postService.getPost(
          match.getMatchedPostId().getPostId());
      ManitoLetterResponseDto existingLetter = manitoService.getLetterByMatchIdAndNickname(
          match.getManitoMatchesId(),
          match.getMatchedUserId().getNickname()
      );

      model.addAttribute("todaysPost", todaysPost);
      model.addAttribute("existingLetter", existingLetter);
      model.addAttribute("currentMatch", match);
      model.addAttribute("canRequestMatch", false);
    } catch (Exception e) {
      log.error("Error loading match data", e);
      model.addAttribute("canRequestMatch", false);
      model.addAttribute("errorMessage", "매칭 정보를 불러오는 중 오류가 발생했습니다.");
    }
  }

  // 타임아웃된 프로세스 처리
  private void handleTimedOutProcess(MatchProcessStatus status, Model model) {
    status.fail();
    statusRepository.save(status);
    checkMatchAvailability(status.getNickname(), model);
  }

  // 진행 중인 매칭 처리
  private void handleInProgressMatch(String nickname, Model model) {
    try {
      CompletableFuture<ManitoMatches> futureMatch = asyncMatchService.processMatchAsync(nickname);

      // 비동기 매칭 진행 중임을 표시
      model.addAttribute("matchProcessing", true);
      model.addAttribute("canRequestMatch", false);

      // 추가적인 처리가 필요한 경우를 위한 상태 정보
      model.addAttribute("processingStatus", "매칭이 진행 중입니다.");
    } catch (Exception e) {
      log.error("Error processing async match for user: {}", nickname, e);
      model.addAttribute("canRequestMatch", true);
      model.addAttribute("errorMessage", "매칭 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
    }
  }

  // 새로운 매칭 가능 여부 확인
  private void checkMatchAvailability(String nickname, Model model) {
    if (manitoMatchesService.hasRecentMatch(nickname, LocalDateTime.now().minusHours(24))) {
      model.addAttribute("canRequestMatch", false);
      model.addAttribute("errorMessage", "24시간 이내에 이미 매칭을 받으셨습니다.");
    } else {
      model.addAttribute("canRequestMatch", true);
    }
  }

  // 매칭 요청을 처리하는 API 엔드포인트 추가
  @PostMapping("/api/match/request")
  @ResponseBody
  public ResponseEntity<?> requestMatch(HttpSession session) {
    String nickname = (String) session.getAttribute("nickname");
    if (nickname == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse("로그인이 필요합니다."));
    }

    try {
      // 현재 유효한 매칭이 있는지 확인
      Optional<ManitoMatches> existingMatch = manitoMatchesService.getCurrentValidMatch(nickname);
      if (existingMatch.isPresent()) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("이미 진행 중인 매칭이 있습니다."));
      }

      // 24시간 이내 매칭 여부 확인
      if (manitoMatchesService.hasRecentMatch(nickname, LocalDateTime.now().minusHours(24))) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("24시간 이내에 이미 매칭을 받으셨습니다."));
      }

      asyncMatchService.processMatchAsync(nickname);
      return ResponseEntity.ok().body(new SuccessResponse("매칭이 시작되었습니다."));
    } catch (Exception e) {
      log.error("Error requesting match for user: {}", nickname, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("매칭 요청 처리 중 오류가 발생했습니다."));
    }
  }

  // 매칭 상태를 확인하는 API 엔드포인트 추가
  @GetMapping("/api/match/status")
  @ResponseBody
  public ResponseEntity<?> checkMatchStatus(HttpSession session) {
    String nickname = (String) session.getAttribute("nickname");
    if (nickname == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ErrorResponse("로그인이 필요합니다."));
    }

    try {
      Optional<MatchProcessStatus> status = statusRepository
          .findFirstByNicknameAndStatusOrderByCreatedAtDesc(
              nickname,
              MatchProcessStatus.ProcessStatus.IN_PROGRESS
          );

      if (status.isEmpty()) {
        return ResponseEntity.ok().body(new MatchStatusResponse("COMPLETED", null));
      }

      MatchProcessStatus processStatus = status.get();
      if (processStatus.isTimedOut()) {
        processStatus.fail();
        statusRepository.save(processStatus);
        return ResponseEntity.ok().body(
            new MatchStatusResponse("FAILED", "매칭 시간이 초과되었습니다.")
        );
      }

      String statusValue = processStatus.getStatus().toString();
      return ResponseEntity.ok().body(new MatchStatusResponse(statusValue, null));

    } catch (Exception e) {
      log.error("Error checking match status for user: {}", nickname, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("매칭 상태 확인 중 오류가 발생했습니다."));
    }
  }

  // 응답 객체들
  @Getter
  @AllArgsConstructor
  private static class ErrorResponse {
    private String message;
  }

  @Getter
  @AllArgsConstructor
  private static class SuccessResponse {
    private String message;
  }

  @Getter
  @AllArgsConstructor
  private static class MatchStatusResponse {
    private String status;
    private String message;
  }
}