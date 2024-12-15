package com.finalproject.manitoone.domain.dto.admin;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyPostSearchResponseDto {

  private Long replyPostId;
  private PostSearchResponseDto post;
  private UserSearchResponseDto user;
  private Long parentId;
  private String content;
  private LocalDateTime createdAt;
  private Boolean isBlind;
  private String timeDifference;
}
