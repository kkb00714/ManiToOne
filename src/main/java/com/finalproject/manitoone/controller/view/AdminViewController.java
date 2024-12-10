package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.Role;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

  @GetMapping("/posts")
  public String adminPosts() {
    return "pages/admin/adminPost";
  }

  @GetMapping("/reports")
  public String adminReports(Model model) {
    List<Map<String, String>> types = Arrays.stream(ReportObjectType.values())
        .filter(type -> type == ReportObjectType.POST || type == ReportObjectType.REPLY) // 필터링
        .map(type -> Map.of(
            "value", type.name(),
            "label", type.getType()
        ))
        .toList();

    model.addAttribute("types", types); // 필터 데이터 추가
    return "pages/admin/adminReport";
  }
}
