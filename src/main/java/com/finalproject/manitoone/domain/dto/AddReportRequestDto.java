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
public class AddReportRequestDto {

  private ReportType reportType;
  private ReportObjectType reportObjectType;
}
