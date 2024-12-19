package com.finalproject.manitoone.dto.post;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiPostLogResponseDto {

  private String content;
  private String musicTitle;
  private String musicLink;
}
