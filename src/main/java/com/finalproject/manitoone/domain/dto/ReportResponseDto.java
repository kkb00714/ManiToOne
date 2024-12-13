package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.ReportType;
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
  ReportType reportType;  // 신고 사유
  ReportObjectType type;  // 게시글, 답글, 마니또 여부
}
