package com.finalproject.manitoone.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IllegalActionMessages {
  CANNOT_FIND_USER_WITH_GIVEN_ID("해당하는 유저 ID를 찾을 수 없습니다."),
  CANNOT_FIND_POST_WITH_GIVEN_ID("해당하는 게시글 ID를 찾을 수 없습니다."),
  CANNOT_FIND_REPLY_WITH_GIVEN_ID("해당하는 답글 ID를 찾을 수 없습니다."),
  CANNOT_FIND_REPORT_WITH_GIVEN_ID("해당하는 신고 ID를 찾을 수 없습니다."),
  CANNOT_FIND_POST_IMAGE_WITH_GIVEN_ID("해당 게시물은 이미지를 포함하고 있지 않습니다."),
  CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID("해당 게시물은 답글이 없습니다."),
  CANNOT_FIND_USER_POST_LIKE_WITH_GIVEN_ID("해당 게시물을 좋아하는 사람이 없습니다."),
  CANNOT_FIND_MANITO_LETTER_WITH_GIVEN_ID("해당 게시물에 대한 마니또 편지가 없습니다."),
  CANNOT_FIND_MANITO_WITH_GIVEN_ID("해당하는 마니또를 찾을 수 없습니다."),
  CANNOT_FIND_MANITO_MATCH_WITH_GIVEN_ID("해당 게시물은 마니또가 매칭되지 않았습니다."),
  CANNOT_FIND_AI_POST_LOG("해당 게시물에 대한 AI 포스트 요약이 없습니다"),
  CANNOT_FIND_ANY_POST("작성된 게시물이 없습니다."),
  CANNOT_FIND_ANY_NOTIFICATIONS("해당 게시글에 대한 알림이 없습니다."),
  CANNOT_DELETE_POST_AND_REPLY("타인의 게시글을 삭제할 수 없습니다"),
  CANNOT_FIND_USER_BY_GIVEN_NICKNAME("해당 유저의 닉네임을 찾을 수 없습니다."),
  CANNOT_GET_FOLLOWERS("팔로워 정보를 가져오는 중 문제가 발생했습니다."),
  CANNOT_GET_FOLLOWING("팔로잉 정보를 가져오는 중 문제가 발생했습니다."),
  CANNOT_FOLLOW_YOURSELF("자기 자신은 팔로우 할 수 없습니다."),
  CANNOT_HIDE_POST("타인의 게시글은 숨김 처리할 수 없습니다."),
  CANNOT_SAVE_IMAGE("이미지를 저장할 수 없습니다."),
  CANNOT_FIND_NOTIFICATION("알림을 찾을 수 없습니다."),

  DIFFERENT_USER("해당 게시물의 작성자와 ID가 다릅니다."),

  // Auth
  USER_NOT_FOUND("해당하는 유저를 찾을 수 없습니다."),
  USER_ALREADY_IN_USE("이미 존재하는 유저입니다."),
  EMAIL_ALREADY_IN_USE("이미 사용 중인 이메일입니다"),
  NICKNAME_ALREADY_IN_USE("이미 사용 중인 닉네임입니다"),
  EMAIL_VERIFICATION_FAILED("이메일 인증에 실패했습니다."),
  EMAIL_VERIFICATION_CODE_MISMATCH("이메일 인증번호가 일치하지 않습니다."),
  INVALID_EMAIL_OR_PASSWORD("이메일 및 비밀번호가 일치하지 않습니다."),
  FAILED_LOGIN("로그인에 실패했습니다."),
  FAILED_LOGOUT("로그아웃에 실패했습니다."),

  ACCESS_DENIED_PROHIBITED_USER("정지된 계정입니다. 관리자에게 문의하세요."),
  ACCESS_DENIED_EXPIRED_USER("탈퇴된 계정입니다. 관리자에게 문의하세요."),
  UNAUTORIZED("권한이 없습니다."),

  SESSION_REQUIRED("로그인이 필요합니다.");

  private final String message;
}
