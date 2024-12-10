package com.finalproject.manitoone.constants;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {
  // 신고 사유
  DISLIKE("마음에 들지 않습니다"),
  HARASSMENT("따돌림 또는 원치 않은 연락"),
  SELF_HARM("자살, 자해 및 섭식 장애"),
  ABUSE("폭력, 혐오 또는 학대"),
  RESTRICTED_ITEMS("제한된 품목을 판매하거나 홍보함"),
  ADULT_CONTENT("나체 이미지 또는 성적 행위"),
  SCAM("스캠, 사기 또는 스팸"),
  MISINFO("거짓 정보");

  private final String type;

  public static Map<String, String> toMap() {
    return Arrays.stream(ReportType.values())
        .collect(Collectors.toMap(Enum::name, ReportType::getType));
  }
}
