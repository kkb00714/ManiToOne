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
public class PostSearchResponseDto {

  private Long postId;
  private UserSearchRequestDto user;
  private String content;
  private LocalDateTime createdAt = LocalDateTime.now();
  private LocalDateTime updatedAt;
  private Boolean isManito = false;
  private Boolean isSelected = false;
  private Boolean isHidden = false;
  private Boolean isBlind = false;
}
