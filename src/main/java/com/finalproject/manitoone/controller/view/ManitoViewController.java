package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.dto.manito.ManitoLetterResponseDto;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.service.ManitoMatchesService;
import com.finalproject.manitoone.service.ManitoService;
import com.finalproject.manitoone.service.PostService;
import jakarta.servlet.http.HttpSession;
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

      // 현재 유효한 매칭 확인 (24시간 이내의 매칭)
      Optional<ManitoMatches> currentMatch = manitoMatchesService.getCurrentValidMatch(nickname);

      if (currentMatch.isPresent()) {
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
        model.addAttribute("canRequestMatch", true);
      }

      return "pages/manito";

    } catch (Exception e) {
      log.error("Error in getManitoPage: ", e);
      model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다.");
      return "pages/manito";
    }
  }
}