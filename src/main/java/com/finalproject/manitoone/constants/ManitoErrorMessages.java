package com.finalproject.manitoone.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ManitoErrorMessages {
  // 엔티티 검사
  POST_NOT_FOUND("게시물을 찾을 수 없습니다."),
  USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
  MANITO_LETTER_NOT_FOUND("편지를 찾을 수 없습니다."),
  ANSWER_NOT_FOUND("답장이 존재하지 않습니다."),

  // 유효성 검사
  NOT_MANITO_POST("마니또 게시물이 아닙니다."),
  NOT_SELECTED_POST("유저가 마니또 게시물로 지정하지 않은 게시물입니다."),
  OWN_POST_LETTER("자신의 게시물에는 마니또 편지를 작성할 수 없습니다."),
  OWN_LETTER_REPORT("자신의 편지는 신고할 수 없습니다."),
  OWN_ANSWER_REPORT("자신의 답장은 신고할 수 없습니다."),
  ALREADY_REPLIED("이미 편지를 작성하셨습니다."),
  ALREADY_ANSWERED("이미 편지에 대한 답장을 작성하셨습니다."),
  ALREADY_REPORTED("이미 신고된 편지입니다."),
  ALREADY_REPORTED_ANSWER("이미 신고된 답장입니다."),

  // 권한 검사
  NO_PERMISSION_REPLY("답장을 작성할 권한이 없습니다."),
  NO_PERMISSION_REPORT("신고할 권한이 없습니다."),
  NO_PERMISSION_VISIBILITY("공개 설정을 변경할 권한이 없습니다."),
  NO_PERMISSION_MAILBOX("자신의 편지함만 볼 수 있습니다."),
  NO_PERMISSION_LETTER("해당 편지에 대한 접근 권한이 없습니다.");

  private final String message;

}
