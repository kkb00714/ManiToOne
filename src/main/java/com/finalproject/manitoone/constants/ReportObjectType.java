package com.finalproject.manitoone.constants;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
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

  public static Map<String, String> toMap() {
    return Arrays.stream(ReportObjectType.values())
        .collect(Collectors.toMap(Enum::name, ReportObjectType::getType));
  }
}
