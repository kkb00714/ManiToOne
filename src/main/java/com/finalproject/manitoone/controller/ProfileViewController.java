package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.service.FollowService;
import com.finalproject.manitoone.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/profile")
public class ProfileViewController {

  private final UserService userService;
  private final FollowService followService;

  @GetMapping("/{nickname}")
  public String myPage(@PathVariable String nickname, Model model, HttpSession session) {
    if (session.getAttribute("nickname") == null)
      return "redirect:/";
    model.addAttribute("isFollowed",
        followService.isFollowed((String) session.getAttribute("nickname"), nickname));
    model.addAttribute("nickname", session.getAttribute("nickname"));
    model.addAttribute("user", userService.getUserByNickname(nickname));
    return "pages/profile";
  }
}
