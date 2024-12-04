package com.finalproject.manitoone.util;

import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AddNotificationRequestDto;
import com.finalproject.manitoone.domain.dto.NotificationResponseDto;
import com.finalproject.manitoone.repository.NotificationRepository;
import com.finalproject.manitoone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class NotificationUtil {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  public NotificationResponseDto createNotification(
      String receiveUserNickname,
      User sendUser, NotiType type,
      Long relatedObjectId) {
    return new NotificationResponseDto(
        notificationRepository.save(AddNotificationRequestDto.builder()
            .receiveUser(userRepository.findUserByNickname(receiveUserNickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임을 가진 유저를 찾을 수 없습니다.")))
            .sendUser(sendUser)
            .type(type)
            .relatedObjectId(relatedObjectId)
            .build().toEntity()));
  }
}
