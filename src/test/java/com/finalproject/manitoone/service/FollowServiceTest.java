package com.finalproject.manitoone.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.Follow;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.repository.FollowRepository;
import com.finalproject.manitoone.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FollowServiceTest {

  @Mock
  private FollowRepository followRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private FollowService followService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // isFollowed: 나의 닉네임이 존재하지 않는다면 IllegalArgumentException이 던져져야 함.
  @Test
  void shouldThrowIllegalArgumentExceptionWhenMyNickNameDoesNotExist() {
    String myNickName = "nonExistentUser";
    String targetNickName = "existingUser";
    when(userRepository.findUserByNickname(myNickName)).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      followService.isFollowed(myNickName, targetNickName);
    });
    assertEquals(IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage(),
        exception.getMessage());
  }

  // isFollowed: 대상 닉네임이 존재하지 않는다면 IllegalArgumentException이 던져져야 함.
  @Test
  void shouldThrowIllegalArgumentExceptionWhenTargetNickNameDoesNotExist() {
    String myNickName = "existingUser";
    String targetNickName = "nonExistentUser";
    when(userRepository.findUserByNickname(myNickName)).thenReturn(Optional.of(new User()));
    when(userRepository.findUserByNickname(targetNickName)).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      followService.isFollowed(myNickName, targetNickName);
    });
    assertEquals(IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage(),
        exception.getMessage());
  }

  // isFollowed: 메서드를 호출 하였을 때 팔로우 관계가 존재하지 않으면 False를 반환 하여야 함.
  @Test
  void shouldReturnFalseWhenBothNickNamesExistButAreNotFollowed() {
    String myNickName = "myNickname1";
    String targetNickName = "targetNickname1";
    User myUser = new User();
    User targetUser = new User();
    when(userRepository.findUserByNickname(myNickName)).thenReturn(Optional.of(myUser));
    when(userRepository.findUserByNickname(targetNickName)).thenReturn(Optional.of(targetUser));
    when(followRepository.existsByFollower_UserIdAndFollowing_UserId(myUser.getUserId(),
        targetUser.getUserId())).thenReturn(false);

    Boolean result = followService.isFollowed(myNickName, targetNickName);

    assertEquals(false, result);
  }

  // isFollowed: 메서드를 호출 하였을 때 이미 팔로우중이라면 True를 반환 하여야 함.
  @Test
  void shouldReturnTrueWhenMyNickNameIsFollowingTargetNickName() {
    String myNickName = "myNickname1";
    String targetNickName = "targetNickname1";
    User myUser = new User();
    User targetUser = new User();
    when(userRepository.findUserByNickname(myNickName)).thenReturn(Optional.of(myUser));
    when(userRepository.findUserByNickname(targetNickName)).thenReturn(Optional.of(targetUser));
    when(followRepository.existsByFollower_UserIdAndFollowing_UserId(myUser.getUserId(),
        targetUser.getUserId()))
        .thenReturn(true);

    Boolean result = followService.isFollowed(myNickName, targetNickName);

    assertEquals(true, result);
  }

  // toggleFollow: 이미 팔로잉중인 유저를 팔로우 취소 했을 때 False가 반환되어야 함.
  @Test
  void shouldReturnFalseAndDeleteExistingFollowWhenAlreadyFollowingInToggleFollow() {
    String myNickName = "myNickname1";
    String targetNickName = "targetNickname1";
    User myUser = new User();
    User targetUser = new User();
    Follow existingFollow = new Follow(null, myUser, targetUser);

    when(userRepository.findUserByNickname(myNickName)).thenReturn(Optional.of(myUser));
    when(userRepository.findUserByNickname(targetNickName)).thenReturn(Optional.of(targetUser));
    when(followRepository.findByFollower_UserIdAndFollowing_UserId(myUser.getUserId(),
        targetUser.getUserId()))
        .thenReturn(Optional.of(existingFollow));

    Boolean result = followService.toggleFollow(myNickName, targetNickName);

    assertEquals(false, result);
    verify(followRepository).delete(existingFollow);
  }
}
