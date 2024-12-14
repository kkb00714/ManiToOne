package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.PostService;
import com.finalproject.manitoone.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebViewController {

  private final PostService postService;
  private final UserService userService;

  @GetMapping("/")
  public String getIndex(Model model, HttpSession session) {
    String nickname = (String) session.getAttribute("nickname");
    if (nickname == null) {
      return "redirect:/login";
    }
    model.addAttribute("userNickname", nickname);
    model.addAttribute("posts", postService.getTimelinePosts(
        nickname,
        PageRequest.of(0, 20, Sort.by(Direction.DESC, "createdAt"))
    ));
    model.addAttribute("user", userService.getCurrentUser(nickname));
    return "index";
  }
}
