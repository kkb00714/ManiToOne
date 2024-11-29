package com.finalproject.manitoone.service;

import com.finalproject.manitoone.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

//  public AddNotificationRequest createAddNotificationRequest(String receiveUserNickname, User sendUser, NotiType type,
//      Long relatedObjectId) {
//
//    return AddNotificationRequest.builder()
//        .receiveUser(userRepository.findByNickname(receiveUserNickname))
//        .sendUser(userRepository.findById(2L).orElseThrow())
//        .type(type)
//        .relatedObjectId(relatedObjectId)
//        .build();
//  }

//  public NotificationResponse createNotification(AddNotificationRequest addNotificationRequest) {
//    return notificationRepository.save(addNotificationRequest.toEntity()).toResponse();
//  }
//
//  public List<NotificationResponse> getAllUnReadNotifications(User user) {
//    return notificationRepository.findByIsReadAndUser(false, user).stream()
//        .map(Notification::toResponse)
//        .toList();
//  }
}
