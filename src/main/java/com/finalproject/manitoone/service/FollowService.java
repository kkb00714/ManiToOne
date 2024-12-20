package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.Follow;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.follow.FollowResponseDto;
import com.finalproject.manitoone.repository.FollowRepository;
import com.finalproject.manitoone.repository.NotificationRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.NotificationUtil;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;
  private final NotificationUtil notificationUtil;

  public Boolean isFollowed(String myNickName, String targetNickName) {
    User my = userRepository.findUserByNickname(myNickName).orElseThrow(
        () -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));
    User target = userRepository.findUserByNickname(targetNickName).orElseThrow(
        () -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    return followRepository.existsByFollower_UserIdAndFollowing_UserId(my.getUserId(), target.getUserId());
  }

  @Transactional
  public Boolean toggleFollow(String myNickName, String targetNickName) {
    if (myNickName.equals(targetNickName)) {
      throw new IllegalArgumentException(IllegalActionMessages.CANNOT_FOLLOW_YOURSELF.getMessage());
    }

    User my = userRepository.findUserByNickname(myNickName)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    User target = userRepository.findUserByNickname(targetNickName)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    Optional<Follow> existingFollow = followRepository
        .findByFollower_UserIdAndFollowing_UserId(my.getUserId(), target.getUserId());

    if (existingFollow.isPresent()) {
      followRepository.delete(existingFollow.get());
      return Boolean.FALSE;
    } else {
      if (Boolean.FALSE.equals(followRepository.existsByFollower_UserIdAndFollowing_UserId(my.getUserId(), target.getUserId()))) {
        Follow newFollow = new Follow(null, my, target);
        followRepository.save(newFollow);
        try
        {
          Notification notification = notificationRepository.findByUserAndSenderUserAndType(target, my, NotiType.FOLLOW);
          // 이미 팔로우 한 알림이 존재한다면 알림만 업데이트
          if (notification != null) {
            notification.updateCreatedAt();
            notification.unMarkAsRead();
            notificationRepository.save(notification);
            notificationUtil.sendAlarm(target);
          } else {
            notificationUtil.createNotification(target.getNickname(), my, NotiType.FOLLOW, my.getUserId());
          }

        } catch (IOException e) {
          log.error(e.getMessage());
        }
        return Boolean.TRUE;
      }
      return Boolean.FALSE;
    }
  }

  public List<Follow> getFollowings(Long userId) {
    List<Follow> list = followRepository.findAllByFollowing_UserId(userId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_GET_FOLLOWERS.getMessage()
        ));

    return list;
  }
}
