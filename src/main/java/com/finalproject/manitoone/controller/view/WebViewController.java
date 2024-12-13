package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.PostService;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class WebViewController {

  private final PostService postService;

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
    return "index";
  }

  @GetMapping("/api/session-info")
  @ResponseBody
  public ResponseEntity<Map<String, String>> getSessionInfo(HttpSession session) {
    Map<String, String> response = new HashMap<>();
    String nickname = (String) session.getAttribute("nickname");
    response.put("nickname", nickname);
    return ResponseEntity.ok(response);
  }
}
