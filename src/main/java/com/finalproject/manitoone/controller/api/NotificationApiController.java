package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api")
@RequiredArgsConstructor
public class NotificationApiController {

  private final NotificationService notificationService;

  @PutMapping("/notification/{notiId}")
  public ResponseEntity readNotification(@PathVariable Long notiId, HttpServletRequest request) {
    notificationService.readNotification(notiId, request.getSession());
    return ResponseEntity.ok().build();
  }

  @PutMapping("/notification")
  public ResponseEntity<Object> readAllNotification(HttpServletRequest request) {
    notificationService.readAllNotifications(request.getSession());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/notifications/status")
  public ResponseEntity<Object> checkUnreadNotifications(HttpServletRequest request) {
    return ResponseEntity.ok(notificationService.hasUnreadNotifications(
        (String) request.getSession().getAttribute("email")));
  }
}
