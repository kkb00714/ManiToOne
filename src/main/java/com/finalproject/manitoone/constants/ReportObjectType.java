package com.finalproject.manitoone.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportObjectType {
  // 신고 타입 (에: 게시글 신고 or 답글 신고 or 마니또 편지 신고 or 마니또 답장 신고)
  POST("게시글"),
  REPLY("답글"),
  MANITO_LETTER("마니또 편지"),
  MANITO_ANSWER("마니또 답장");

  private final String type;
}
