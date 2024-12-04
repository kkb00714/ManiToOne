package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
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
    User user = userRepository.findUserByNickname(nickname)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_BY_GIVEN_NICKNAME.getMessage()));
    UserInformationResponseDto userInformation = new UserInformationResponseDto(
        userRepository.findUserByNickname(nickname)
            .orElseThrow(() -> new IllegalArgumentException(
                IllegalActionMessages.CANNOT_FIND_USER_BY_GIVEN_NICKNAME.getMessage())));

    List<Follow> followersList = followRepository.findAllByFollower_UserId(user.getUserId())
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_GET_FOLLOWERS.getMessage()));
    List<UserInformationResponseDto> followers = followersList.stream()
        .map(follow -> new UserInformationResponseDto(follow.getFollowing()))
        .toList();

    List<Follow> followingsList = followRepository.findAllByFollowing_UserId(user.getUserId())
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_GET_FOLLOWING.getMessage()));
    List<UserInformationResponseDto> followings = followingsList.stream()
        .map(follow -> new UserInformationResponseDto(follow.getFollower()))
        .toList();

    userInformation.setFollow(followers, followings);

    return userInformation;
  }
}
