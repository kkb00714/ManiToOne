package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AddNotificationRequest;
import com.finalproject.manitoone.domain.dto.NotificationResponse;
import com.finalproject.manitoone.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  public AddNotificationRequest createAddNotificationRequest(User user, NotiType type, Long relatedObjectId) {
    return AddNotificationRequest.builder()
        .user(user)
        .type(type)
        .relatedObjectId(relatedObjectId)
        .build();
  }


  public NotificationResponse createNotification(AddNotificationRequest addNotificationRequest) {
    return notificationRepository.save(addNotificationRequest.toEntity()).toResponse();
  }
}
