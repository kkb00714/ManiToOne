package com.finalproject.manitoone.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

  @GetMapping
  public String adminIndex() {
    return "pages/admin/adminIndex";
  }
}
