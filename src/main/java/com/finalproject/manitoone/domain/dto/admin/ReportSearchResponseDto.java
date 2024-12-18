package com.finalproject.manitoone.domain.dto.admin;

import com.finalproject.manitoone.dto.admin.ManitoSearchResponseDto;
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
  private String letter;
  private UserSearchResponseDto reportedByUser;
  private Long reportObjectId;
  private LocalDateTime createdAt;
  private UserSearchResponseDto reportedToUser;

  // 신고 당한 유저는 해당 Dto에서 찾기
  private PostSearchResponseDto post;
  private ReplyPostSearchResponseDto replyPost;
  private ManitoSearchResponseDto manito;
}