package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.NotificationResponseDto;
import com.finalproject.manitoone.dto.user.UserInformationResponseDto;
import com.finalproject.manitoone.repository.NotificationRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.DataUtil;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
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
    String email;
    if (session.getAttribute("email") == null) {
      throw new IllegalArgumentException(IllegalActionMessages.UNAUTORIZED.getMessage());
    }
    email = (String) session.getAttribute("email");
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.USER_NOT_FOUND.getMessage()));
    if (user == null) {
      throw new IllegalArgumentException(IllegalActionMessages.UNAUTORIZED.getMessage());
    }

    // 모든 알림 읽음 처리
    // fixme: 테스트를 위해 잠시 주석
//    readAllNotifications(user);

    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

    return notificationRepository.findByUserAndCreatedAtAfterOrderByCreatedAtDesc(user,
            thirtyDaysAgo).stream()
        .map(notification -> NotificationResponseDto.builder()
            .notiId(notification.getNotiId())
            .relatedObjectId(notification.getRelatedObjectId())
            .isRead(notification.getIsRead())
            .createdAt(notification.getCreatedAt())
            .user(new UserInformationResponseDto(user.getName(), user.getNickname(),
                user.getIntroduce(), user.getProfileImage()))
            .senderUser(new UserInformationResponseDto(notification.getSenderUser().getName(),
                notification.getSenderUser().getNickname(),
                notification.getSenderUser().getIntroduce(),
                notification.getSenderUser().getProfileImage()))
            .timeDifference(dataUtil.getTimeDifference(notification.getCreatedAt()))
            .type(notification.getType())
            .content(notification.getType().getMessage(notification.getSenderUser().getNickname()))
            .build())
        .toList();
  }

  public void readAllNotifications(User user) {
    List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(user);
    notifications.forEach(Notification::markAsRead);
    notificationRepository.saveAll(notifications);
  }

  // 알림 읽음 단일 처리
  public void readNotification(Long notiId, HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
      throw new IllegalArgumentException(IllegalActionMessages.UNAUTORIZED.getMessage());
    }
    Notification notification = notificationRepository.findById(notiId)
        .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));
    if (!user.getNickname().equals(notification.getUser().getNickname())) {
      throw new IllegalArgumentException(IllegalActionMessages.UNAUTORIZED.getMessage());
    }
    notification.markAsRead();
    notificationRepository.save(notification);
  }

  // 알림 읽음 모두 처리


  public boolean hasUnreadNotifications(String email) {
    return notificationRepository.existsByUserEmailAndIsRead(email, false);
  }
}
