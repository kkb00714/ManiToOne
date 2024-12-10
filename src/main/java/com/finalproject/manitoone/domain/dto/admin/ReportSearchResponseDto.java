package com.finalproject.manitoone.domain.dto.admin;

import java.time.LocalDateTime;
import java.util.Map;
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
  private Map<String, String> type;
  private Map<String, String> reportType;
  private String content;
  private UserSearchResponseDto reportedByUser;
  private Long reportObjectId;
  private LocalDateTime createdAt;

  // 신고 당한 유저는 해당 Dto에서 찾기
  private PostSearchResponseDto post;
  private ReplyPostSearchResponseDto replyPost;
}
