package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebViewController {

  private final PostService postService;

  @GetMapping("/")
  public String getIndex(Model model, @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      return "redirect:/login";
    }
    String nickname = userDetails.getUsername();
    model.addAttribute("userNickname", nickname);
    model.addAttribute("posts", postService.getTimelinePosts(
        nickname,
        PageRequest.of(0, 20, Sort.by(Direction.DESC, "createdAt"))
    ));
    return "index";
  }
}
