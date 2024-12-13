package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.constants.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
  @NotNull
  private ReportType reportType;
}
