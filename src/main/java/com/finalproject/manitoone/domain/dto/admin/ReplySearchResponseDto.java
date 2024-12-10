package com.finalproject.manitoone.domain.dto.admin;

import java.time.LocalDateTime;

public class ReplySearchResponseDto {

  private Long replyPostId;
  private PostSearchResponseDto post;
  private UserSearchResponseDto user;
  private Long parentId;
  private String content;
  private LocalDateTime createdAt;
  private Boolean isBlind;
}
