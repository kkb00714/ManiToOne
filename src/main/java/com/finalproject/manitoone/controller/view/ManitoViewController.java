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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        // 현재 유효한 매칭이 있는 경우
        ManitoMatches match = currentMatch.get();
        try {
          PostViewResponseDto todaysPost = postService.getPost(
              match.getMatchedPostId().getPostId());
          ManitoLetterResponseDto existingLetter = manitoService.getLetterByMatchIdAndNickname(
              match.getManitoMatchesId(),
              nickname
          );

          model.addAttribute("todaysPost", todaysPost);
          model.addAttribute("existingLetter", existingLetter);
          model.addAttribute("currentMatch", match);
          model.addAttribute("canRequestMatch", false);
        } catch (Exception e) {
          log.error("Error loading match data", e);
          model.addAttribute("canRequestMatch", false);
        }
      } else {
        // 진행 중인 매칭 프로세스 확인
        Optional<MatchProcessStatus> inProgressStatus = statusRepository
            .findFirstByNicknameAndStatusOrderByCreatedAtDesc(
                nickname,
                MatchProcessStatus.ProcessStatus.IN_PROGRESS
            );

        if (inProgressStatus.isPresent()) {
          model.addAttribute("matchProcessing", true);
          model.addAttribute("canRequestMatch", false);
        } else {
          // 24시간 이내 매칭 여부 확인 - 서비스 메서드 사용
          if (manitoMatchesService.hasRecentMatch(nickname, LocalDateTime.now().minusHours(24))) {
            model.addAttribute("canRequestMatch", false);
            model.addAttribute("errorMessage", "24시간 이내에 이미 매칭을 받으셨습니다.");
          } else {
            model.addAttribute("canRequestMatch", true);
          }
        }
      }

      return "pages/manito";

    } catch (Exception e) {
      log.error("Error in getManitoPage: ", e);
      model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다.");
      return "pages/manito";
    }
  }
}