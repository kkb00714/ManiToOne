package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.dto.manito.ManitoLetterResponseDto;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.service.ManitoService;
import com.finalproject.manitoone.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/manito")
@RequiredArgsConstructor
public class ManitoViewController {

  private final ManitoService manitoService;
  private final PostRepository postRepository;
  private final PostService postService;

  @GetMapping("/fragments/manito-letter")
  public String getManitoLetterFragment(@RequestParam Long letterId, Model model) {
    ManitoLetterResponseDto letter = manitoService.getLetter(letterId);
    model.addAttribute("letter", letter);
    return "fragments/common/manito-letter :: manito-letter(letter=${letter})";
  }

  // 마니또 게시글 배당 로직 설정 전 임시
  @GetMapping
  public String getManitoPage(HttpSession session, Model model) {
    String nickname = (String) session.getAttribute("nickname");
    if (nickname == null) {
      return "redirect:/login";
    }

    PostViewResponseDto todaysPost = postService.getPost(108L);
    ManitoLetterResponseDto existingLetter = manitoService.getLetterByPostIdAndNickname(108L,
        nickname);

    model.addAttribute("userNickname", nickname);
    model.addAttribute("todaysPost", todaysPost);
    model.addAttribute("existingLetter", existingLetter);
    return "pages/manito";
  }
}
