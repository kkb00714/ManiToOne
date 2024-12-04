package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.Follow;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.repository.FollowRepository;
import com.finalproject.manitoone.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;

  public Boolean toggleFollow(Long userId, Long targetUserId) {

    if (userId.equals(targetUserId)) {
      throw new IllegalArgumentException(IllegalActionMessages.CANNOT_FOLLOW_YOURSELF.getMessage());
    }

    Optional<Follow> follow = followRepository.findByFollower_UserIdAndFollowing_UserId(userId,
        targetUserId);

    if (follow.isPresent()) {
      followRepository.delete(follow.get());
      return Boolean.FALSE;
    } else {
      User my = userRepository.findById(userId)
          .orElseThrow(() -> new IllegalArgumentException(
              IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));
      User target = userRepository.findById(targetUserId)
          .orElseThrow(() -> new IllegalArgumentException(
              IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));
      Follow newFollow = new Follow(null, my, target);
      followRepository.save(newFollow);
      return Boolean.TRUE;
    }
  }
}
