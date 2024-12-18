package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.TimelineService;
import com.finalproject.manitoone.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebViewController {

  private final TimelineService timelineService;
  private final UserService userService;
  private static final int DEFAULT_RECENT_DAYS = 7;

  @GetMapping("/")
  public String getIndex(Model model,
      HttpSession session,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Boolean isNewUser = (Boolean) session.getAttribute("isNewUser");

    if (Boolean.TRUE.equals(isNewUser)) {
      return "/pages/auth/additional-info";
    }

    String nickname = (String) session.getAttribute("nickname");
    if (nickname == null) {
      return "redirect:/login";
    }
    model.addAttribute("userNickname", nickname);
    model.addAttribute("posts", timelineService.getTimelinePosts(
        nickname,
        pageable,
        DEFAULT_RECENT_DAYS
    ));
    model.addAttribute("user", userService.getCurrentUser(nickname));
    return "index";
  }

}
