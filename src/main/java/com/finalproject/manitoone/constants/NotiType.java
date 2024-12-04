package com.finalproject.manitoone.constants;

public enum NotiType {

  POST_REPLY("%s 님이 내 게시물에 답글을 남겼습니다."),
  POST_RE_REPLY("%s 님이 내 답글에 답글을 남겼습니다."),
  FOLLOW("%s 님이 나를 팔로우했습니다."),
  LIKE_CLOVER("%s 님이 내 게시물에 클로버☘를 보냈습니다."),
  RECEIVE_MANITO("두근두근! 나는 오늘 누구의 마니또가 되었을까요? 게시물을 확인하고 격려와 칭찬을 남기러 가요! "),
  MANITO_COMMENT("내 게시물에 마니또가 답글을 남겼습니다. 어서 확인해봐요! "),
  MANITO_THANK_COMMENT("내가 남긴 마니또 답글에 감사인사가 전해졌습니다. 어서 확인해봐요!");

  private final String message;

  NotiType(String message) {
    this.message = message;
  }

  public String getMessage(String userName) {
    return String.format(message, userName);
  }

  public boolean requiresUserName() {
    return message.contains("%s");
  }
}
