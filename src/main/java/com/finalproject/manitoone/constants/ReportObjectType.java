package com.finalproject.manitoone.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportObjectType {
  // 신고 타입 (에: 게시글 신고 or 답글 신고 or 마니또 신고)
  POST("게시글"),
  REPLY("답글"),
  MANITO("마니또");

  private final String type;
}
