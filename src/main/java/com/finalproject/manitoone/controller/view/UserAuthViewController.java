package com.finalproject.manitoone.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserAuthViewController {

  @GetMapping("/register")
  public String getSignUpPage() {
    return "pages/auth/email-auth";
  }

  @GetMapping("/register-info")
  public String getInfoInputPage() {
    return "pages/auth/sign-up";
  }

  @GetMapping("/login")
  public String getSignInPage() {
    return "pages/auth/sign-in";
  }

  @GetMapping("/additional-info")
  public String getAdditionalInfoPage() {
    return "pages/auth/additional-info";
  }

  @GetMapping("/login-fail")
  public String getFailPage() {
    return "pages/auth/oauth-login-failure";
  }

  @GetMapping("/access-deny")
  public String getAccessDenyPage() {
    return "pages/auth/access-deny";
  }

  @GetMapping("/cancel-account")
  public String getCancelAccountPage() {
    return "pages/auth/cancel-account";
  }
}
