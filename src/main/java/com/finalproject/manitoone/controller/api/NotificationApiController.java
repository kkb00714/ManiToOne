package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api")
@RequiredArgsConstructor
public class NotificationApiController {

  private final NotificationService notificationService;

  @PutMapping("/notification/{notiId}")
  public ResponseEntity readNotification(@PathVariable Long notiId) {
    notificationService.readNotification(notiId);
    return ResponseEntity.ok().build();
  }
}
