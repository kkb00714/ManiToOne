package com.finalproject.manitoone.domain.dto.admin;

import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.ReportType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportSearchRequestDto {

  private String reportedBy;
  private String reportedTo;
  private String content;
  private ReportObjectType type;
  private ReportType reportType;
  private Long reportObjectId;
}
