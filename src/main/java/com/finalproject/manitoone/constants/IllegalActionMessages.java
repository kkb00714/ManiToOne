package com.finalproject.manitoone.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IllegalActionMessages {
  CANNOT_FIND_USER_WITH_GIVEN_ID("해당하는 유저 ID를 찾을 수 없습니다."),
  CANNOT_FIND_POST_WITH_GIVEN_ID("해당하는 게시글 ID를 찾을 수 없습니다."),
  CANNOT_FIND_POST_IMAGE_WITH_GIVEN_ID("해당 게시물은 이미지를 포함하고 있지 않습니다."),
  CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID("해당 게시물은 답글이 없습니다."),
  CANNOT_FIND_USER_POST_LIKE_WITH_GIVEN_ID("해당 게시물을 좋아하는 사람이 없습니다."),
  CANNOT_FIND_USER_BY_GIVEN_NICKNAME("해당하는 유저의 닉네임을 찾을 수 없습니다."),
  CANNOT_GET_FOLLOWERS("팔로워 정보를 가져오는 중 문제가 발생했습니다."),
  CANNOT_GET_FOLLOWING("팔로잉 정보를 가져오는 중 문제가 발생했습니다."),
  CANNOT_FOLLOW_YOURSELF("자기 자신은 팔로우 할 수 없습니다."),

  // Auth
  CANNOT_FIND_ANY_USER("해당하는 유저를 찾을 수 없습니다."),
  CANNOT_USE_EMAIL("이미 사용 중인 이메일입니다"),
  CANNOT_USE_NICKNAME("이미 사용 중인 닉네임입니다"),
  CANNOT_VERIFY_EMAIL("이메일 인증에 실패했습니다."),
  CANNOT_VERIFY_EMAIL_NUMBER("이메일 인증번호가 일치하지 않습니다."),
  CANNOT_FIND_EMAIL_OR_PASSWORD("이메일 및 비밀번호가 일치하지 않습니다."),
  CANNOT_SUCCESS_LOGIN("로그인에 실패했습니다.");

  private final String message;
}
