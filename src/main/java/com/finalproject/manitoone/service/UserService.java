package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.Follow;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.user.UserInformationResponseDto;
import com.finalproject.manitoone.repository.FollowRepository;
import com.finalproject.manitoone.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  public UserInformationResponseDto getUserByNickname(String nickname) {
    //TODO: 재사용 가능한 메시지들은 Enum으로 빼서 처리하기.
    User user = userRepository.findUserByNickname(nickname)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 유저의 닉네임을 찾을 수 없습니다."));
    UserInformationResponseDto userInformation = userRepository.findUserByNickname(nickname)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 유저의 닉네임을 찾을 수 없습니다."))
        .toUserInformationDto();

    List<Follow> followersList = followRepository.findAllByFollower_UserId(user.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("팔로워 정보를 가져오는데에 실패했습니다."));
    List<UserInformationResponseDto> followers = followersList.stream()
        .map(follow -> follow.getFollowing().toUserInformationDto())
        .toList();

    List<Follow> followingsList = followRepository.findAllByFollowing_UserId(user.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("팔로잉 정보를 가져오는데에 실패했습니다."));
    List<UserInformationResponseDto> followings = followingsList.stream()
        .map(follow -> follow.getFollower().toUserInformationDto())
        .toList();

    userInformation.setFollow(followers, followings);

    return userInformation;
  }
}
