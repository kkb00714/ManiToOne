package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AddNotificationRequestDto;
import com.finalproject.manitoone.domain.dto.NotificationResponseDto;
import com.finalproject.manitoone.repository.NotificationRepository;
import com.finalproject.manitoone.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  public NotificationResponseDto createNotification(
      String receiveUserNickname,
      User sendUser, NotiType type,
      Long relatedObjectId) {
    return notificationRepository.save(AddNotificationRequestDto.builder()
        .receiveUser(userRepository.findUserByNickname(receiveUserNickname)
            .orElseThrow(() -> new IllegalArgumentException("해당 닉네임을 가진 유저를 찾을 수 없습니다.")))
        .sendUser(userRepository.findById(2L).orElseThrow())
        .type(type)
        .relatedObjectId(relatedObjectId)
        .build().toEntity()).toResponse();
  }

  public List<NotificationResponseDto> getAllUnReadNotifications(User user) {
    return notificationRepository.findByIsReadAndUser(false, user).stream()
        .map(Notification::toResponse)
        .toList();
  }
}
