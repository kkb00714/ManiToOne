package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/noti")
@RequiredArgsConstructor
public class NotificationViewController {

  private final NotificationService notificationService;

  @GetMapping
  public String getNotifications(Model model, HttpServletRequest request) {
    model.addAttribute("notifications", notificationService.getAllUnReadNotifications(request.getSession()));
    return "/pages/notifications";
  }
}
