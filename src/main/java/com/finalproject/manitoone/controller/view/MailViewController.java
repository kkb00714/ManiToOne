package com.finalproject.manitoone.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MailViewController {

  @GetMapping("/find-password")
  public String getResetPage() {
    return "/pages/auth/find-password";
  }
}
