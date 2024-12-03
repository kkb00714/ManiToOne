package com.finalproject.manitoone.dto.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finalproject.manitoone.dto.postimage.PostImageResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDto {

  private Long postId;
  private Long userId;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<PostImageResponseDto> postImages;
  private Integer likeCount;

  public void addPostImages(List<PostImageResponseDto> postImages) {
    this.postImages = postImages;
  }

  public void addLikeCount(Integer likeCount) {
    this.likeCount = likeCount;
  }
}
