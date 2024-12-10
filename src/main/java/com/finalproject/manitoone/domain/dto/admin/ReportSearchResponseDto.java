package com.finalproject.manitoone.domain.dto.admin;

import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.ReportType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSearchResponseDto {

  private Long reportId;
  private ReportObjectType type;
  private String content;
  private ReportType reportType;
  private UserSearchResponseDto reportedByUser;
  private Long reportObjectId;
  private LocalDateTime createdAt;

  // 신고 당한 유저는 해당 Dto에서 찾기
  private PostSearchResponseDto post;
  private ReplySearchResponseDto replyPost;
}
