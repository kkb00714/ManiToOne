package com.finalproject.manitoone.controller.view;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAuthViewController {

  @GetMapping("/register")
  public String getSignUpPage() {
    return "/pages/auth/sign-up";
  }

  @GetMapping("/login")
  public String getSignInPage() {
    return "/pages/auth/sign-in";
  }
}
