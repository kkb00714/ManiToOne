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
    UserInformationResponseDto userInformation = new UserInformationResponseDto(user.getName(),
        user.getNickname(), user.getIntroduce(), user.getProfileImage());

    List<Follow> followersList = followRepository.findAllByFollower_UserId(user.getUserId())
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_GET_FOLLOWERS.getMessage()));
    List<UserInformationResponseDto> followers = followersList.stream()
        .map(follow -> new UserInformationResponseDto(follow.getFollowing().getName(),
            follow.getFollowing().getNickname(), follow.getFollowing().getIntroduce(),
            follow.getFollowing().getProfileImage()))
        .toList();

    List<Follow> followingsList = followRepository.findAllByFollowing_UserId(user.getUserId())
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_GET_FOLLOWING.getMessage()));
    List<UserInformationResponseDto> followings = followingsList.stream()
        .map(follow -> new UserInformationResponseDto(follow.getFollower().getName(),
            follow.getFollower().getNickname(), follow.getFollower().getIntroduce(),
            follow.getFollower().getProfileImage()))
        .toList();

    userInformation.setFollow(followers, followings);

    return userInformation;
  }
}
