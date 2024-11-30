package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPostRequestDto {

  private String content;
  private Boolean isManito;

  public Post toEntity() {
    return Post.builder()
        .content(this.content)
        .isManito(this.isManito)
        .build();
  }
}
