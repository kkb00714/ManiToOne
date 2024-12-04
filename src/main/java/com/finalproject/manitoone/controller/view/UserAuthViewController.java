package com.finalproject.manitoone.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserAuthViewController {

  // TODO 회원가입 시 이메일 인증을 우선적으로 해야 함.
  @GetMapping("/register")
  public String getSignUpPage() {
    return "/pages/auth/sign-up";
  }

  @GetMapping("/login")
  public String getSignInPage() {
    return "/pages/auth/sign-in";
  }
}
