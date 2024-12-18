package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.PostImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImageResponseDto {

  private Long postImageId;
  private Post post;
  private String fileName;
}
