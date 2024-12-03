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

  public PostResponseDto(Post post) {
    this.postId = post.getPostId();
    this.user = post.getUser();
    this.content = post.getContent();
    this.createdAt = post.getCreatedAt();
    this.updatedAt = post.getUpdatedAt();
    this.isManito = post.getIsManito();
    this.isSelected = post.getIsSelected();
    this.isHidden = post.getIsHidden();
    this.isBlind = post.getIsBlind();
  }
}
