package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.constants.Role;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

  @GetMapping
  public String adminIndex() {
    return "pages/admin/adminIndex";
  }

  @GetMapping("/users")
  public String adminUser(Model model) {
    model.addAttribute("roles", Arrays.stream(Role.values())
        .map(Enum::name)
        .toList());
    return "pages/admin/adminUser";
  }
}
