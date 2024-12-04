package com.finalproject.manitoone.controller;

import com.finalproject.manitoone.service.UserService;
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

  @GetMapping("/{nickname}")
  public String myPage(@PathVariable String nickname, Model model) {
    model.addAttribute("user", userService.getUserByNickname(nickname));
    return "profile";
  }
}
