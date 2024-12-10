package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.dto.manito.ManitoLetterResponseDto;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.service.ManitoService;
import com.finalproject.manitoone.service.PostService;
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

  @GetMapping
  public String getManitoPage(Model model) {
    PostViewResponseDto todaysPost = postService.getPost(107L); // post_id 107인 게시글 조회
    model.addAttribute("todaysPost", todaysPost);
    return "pages/manito";
  }
}
