package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.Follow;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.follow.FollowRequestDto;
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
      throw new IllegalArgumentException("자시 자신은 팔로우 할 수 없습니다.");
    }

    Optional<Follow> follow = followRepository.findByFollower_UserIdAndFollowing_UserId(userId,
        targetUserId);

    if (follow.isPresent()) {
      followRepository.delete(follow.get());
      return Boolean.FALSE;
    } else {
      // TODO: 재사용 가능해 보이는 메시지를 Enum 클래스에 정의하기
      User my = userRepository.findById(userId)
          .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 유저를 찾을 수 없습니다."));
      User target = userRepository.findById(targetUserId)
          .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 유저를 찾을 수 없습니다."));
      Follow newFollow = new Follow(null, my, target);
      followRepository.save(newFollow);
      return Boolean.TRUE;
    }
  }
}
