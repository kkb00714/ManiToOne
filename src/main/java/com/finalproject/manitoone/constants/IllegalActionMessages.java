package com.finalproject.manitoone.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IllegalActionMessages {
  CANNOT_FIND_USER_WITH_GIVEN_ID("해당하는 유저 ID를 찾을 수 없습니다."),
  CANNOT_FIND_POST_WITH_GIVEN_ID("해당하는 게시글 ID를 찾을 수 없습니다."),
  CANNOT_FIND_USER_BY_GIVEN_NICKNAME("해당하는 유저의 닉네임을 찾을 수 없습니다."),
  CANNOT_GET_FOLLOWERS("팔로워 정보를 가져오는 중 문제가 발생했습니다."),
  CANNOT_GET_FOLLOWING("팔로잉 정보를 가져오는 중 문제가 발생했습니다."),
  CANNOT_FOLLOW_YOURSELF("자기 자신은 팔로우 할 수 없습니다.");

  private final String message;
}
