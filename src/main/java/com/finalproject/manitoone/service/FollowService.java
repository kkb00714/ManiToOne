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

  public Boolean toggleFollow(String myNickName, String targetNickName) {

    if (myNickName.equals(targetNickName)) {
      throw new IllegalArgumentException(IllegalActionMessages.CANNOT_FOLLOW_YOURSELF.getMessage());
    }

    User my = userRepository.findUserByNickname(myNickName).orElseThrow(
        () -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));
    User target = userRepository.findUserByNickname(targetNickName).orElseThrow(
        () -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    Optional<Follow> follow = followRepository.findByFollower_UserIdAndFollowing_UserId(my.getUserId(),
        target.getUserId());

    if (follow.isPresent()) {
      followRepository.delete(follow.get());
      return Boolean.FALSE;
    } else {
      Follow newFollow = new Follow(null, my, target);
      followRepository.save(newFollow);
      return Boolean.TRUE;
    }
  }
}
