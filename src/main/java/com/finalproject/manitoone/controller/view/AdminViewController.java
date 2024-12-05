package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

  private final AdminService adminService;

  @GetMapping
  public String adminIndex() {
    return "pages/admin/adminIndex";
  }

  @GetMapping("/users")
  public String adminUser() {
    return "pages/admin/adminUser";
  }
}
