package com.finalproject.manitoone.notification.service;

import com.finalproject.manitoone.notification.constants.NotiType;
import com.finalproject.manitoone.notification.domain.dto.AddNotificationRequest;
import com.finalproject.manitoone.notification.domain.dto.NotificationResponse;
import com.finalproject.manitoone.notification.repository.NotificationRepository;
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


  public NotificationResponse createNotification(AddNotificationRequest addNotificationRequest) {
    return notificationRepository.save(addNotificationRequest.toEntity()).toResponse();
  }
}
