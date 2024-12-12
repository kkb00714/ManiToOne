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
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    email = (String) session.getAttribute("email");
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.USER_NOT_FOUND.getMessage()));
    if (user == null) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
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

  public boolean hasUnreadNotifications(Long userId) {
    return notificationRepository.existsByUserUserIdAndIsRead(userId, false);
  }
}
