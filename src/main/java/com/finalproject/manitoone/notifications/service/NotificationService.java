package com.finalproject.manitoone.notifications.service;

import com.finalproject.manitoone.notifications.constants.NotiType;
import com.finalproject.manitoone.notifications.domain.dto.AddNotificationRequest;
import com.finalproject.manitoone.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  public AddNotificationRequest createAddNotificationRequest(Long userId, NotiType type, Long relatedObjectId) {
    return AddNotificationRequest.builder()
        .userId(userId)
        .type(type)
        .relatedObjectId(relatedObjectId)
        .build();
  }
}
