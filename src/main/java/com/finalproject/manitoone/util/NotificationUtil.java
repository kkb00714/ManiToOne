package com.finalproject.manitoone.util;

import com.finalproject.manitoone.aop.AlarmHandler;
import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AddNotificationRequestDto;
import com.finalproject.manitoone.domain.dto.NotificationResponseDto;
import com.finalproject.manitoone.repository.NotificationRepository;
import com.finalproject.manitoone.repository.UserRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class NotificationUtil {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  private final AlarmHandler alarmHandler;

  /**
   * 특정 사용자에게 알림을 생성합니다. (소켓을 통한 알림도 발송)
   *
   * @param receiveUserNickname 알림을 받을 사용자의 닉네임 (예: 팔로우를 받은 유저나 게시글의 주인 등)
   * @param sendUser 알림을 보낸 사용자 (예: 게시글에 좋아요를 누르거나 댓글을 작성한 사용자)
   * @param type 알림의 유형 (예: 게시글 댓글, 팔로우, 좋아요 등, NotiType Enum 참고)
   * @param relatedObjectId 알림을 클릭했을 때 이동할 페이지의 대상 ID (예: 게시글 ID, 사용자 ID 등)
   * @return 생성된 알림 정보를 포함하는 {@link NotificationResponseDto}
   */
  public Notification createNotification(
      String receiveUserNickname,
      User sendUser, NotiType type,
      Long relatedObjectId) throws IOException {
    User receiveUser = userRepository.findUserByNickname(receiveUserNickname)
        .orElseThrow(() -> new IllegalArgumentException("해당 닉네임을 가진 유저를 찾을 수 없습니다."));
    Notification notification = notificationRepository.save(AddNotificationRequestDto.builder()
        .receiveUser(receiveUser)
        .sendUser(sendUser)
        .type(type)
        .relatedObjectId(relatedObjectId)
        .build().toEntity());
    alarmHandler.sendNotification(notification);
    return notification;
  }

  public void sendAlarm(User receiveUser) throws IOException {
    alarmHandler.sendMessage(receiveUser.getEmail(), "알림");
  }
}
