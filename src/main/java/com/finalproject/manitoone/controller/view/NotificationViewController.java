package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/noti")
@RequiredArgsConstructor
public class NotificationViewController {

  private final NotificationService notificationService;

  @GetMapping("/{nickname}")
  public String getNotifications(@PathVariable String nickname, Model model) {
    model.addAttribute("notifications", notificationService.getAllUnReadNotifications(nickname));
    return "/pages/notifications";
  }
}
