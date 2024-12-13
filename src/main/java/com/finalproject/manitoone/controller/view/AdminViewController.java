package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.ReportType;
import com.finalproject.manitoone.constants.Role;
import java.util.Arrays;
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
    model.addAttribute("types", Arrays.stream(ReportObjectType.values())
        .filter(type -> type == ReportObjectType.POST || type == ReportObjectType.REPLY)
        .map(type -> Map.of(
            "value", type.name(),
            "label", type.getType()
        ))
        .toList());
    model.addAttribute("reportTypes", Arrays.stream(ReportType.values())
        .map(type -> Map.of(
            "value", type.name(),
            "label", type.getType()
        ))
        .toList());
    return "pages/admin/adminReport";
  }

  @GetMapping("/manito/reports")
  public String adminManitoReports(Model model) {
    model.addAttribute("types", Arrays.stream(ReportObjectType.values())
        .filter(type -> type == ReportObjectType.MANITO_LETTER || type == ReportObjectType.MANITO_ANSWER)
        .map(type -> Map.of(
            "value", type.name(),
            "label", type.getType()
        ))
        .toList());
    model.addAttribute("reportTypes", Arrays.stream(ReportType.values())
        .map(type -> Map.of(
            "value", type.name(),
            "label", type.getType()
        ))
        .toList());
    return "pages/admin/adminManitoReport";
  }
}
