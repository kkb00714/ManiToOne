package com.finalproject.manitoone.domain.dto.admin;

import com.finalproject.manitoone.constants.ReportObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportStatusResponseDto {
  private boolean isReported;
  private ReportObjectType type;
  private Long reportObjectId;
}
