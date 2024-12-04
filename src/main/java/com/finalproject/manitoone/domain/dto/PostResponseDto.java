package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {

  private Long postId;
  private User user;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean isManito;
  private Boolean isSelected;
  private Boolean isHidden;
  private Boolean isBlind;

  public PostResponseDto(Long postId, String content, Boolean isManito) {
    this.postId = postId;
    this.content = content;
    this.isManito = isManito;
  }
}
