package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.NotificationResponseDto;
import com.finalproject.manitoone.repository.NotificationRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.DataUtil;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  private final DataUtil dataUtil;

  public List<NotificationResponseDto> getAllUnReadNotifications(HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    return notificationRepository.findByIsReadAndUserOrderByNotiIdDesc(false,
            userRepository.findUserByNickname(user.getNickname())
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임을 가진 유저를 찾을 수 없습니다."))).stream()
        .map(notification -> {
          NotificationResponseDto notificationResponseDto = NotificationResponseDto.builder()
              .notiId(notification.getNotiId())
              .relatedObjectId(notification.getRelatedObjectId())
              .isRead(notification.getIsRead())
              .createdAt(notification.getCreatedAt())
              .timeDifference(dataUtil.getTimeDifference(notification.getCreatedAt()))
              .type(notification.getType())
              .build();
          // FIXME: setter를 사용하지 않는 방식으로 content 설정 로직 리팩토링 필요
          if (notificationResponseDto.getType().requiresUserName()) {
            User sendUser = userRepository.findById(notificationResponseDto.getRelatedObjectId())
                .orElseThrow(() -> new IllegalArgumentException("알림을 보낸 유저를 찾을 수 없습니다."));
            notificationResponseDto.setContent(
                notificationResponseDto.getType().getMessage(sendUser.getNickname()));
          } else {
            notificationResponseDto.setContent(notificationResponseDto.getType().getMessage(null));
          }
          return notificationResponseDto;
        })
        .toList();
  }

  public void readNotification(Long notiId, HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    Notification notification = notificationRepository.findById(notiId)
        .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));
    if (!user.getNickname().equals(notification.getUser().getNickname())) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    notification.markAsRead();
    notificationRepository.save(notification);
  }
}
