package com.finalproject.manitoone.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDto {

  Long reportId;
  Long userId;
  Long reportObjectId;
  String reportType; // 신고 사유
  String type;       // 게시글, 답글, 마니또 여부
}
