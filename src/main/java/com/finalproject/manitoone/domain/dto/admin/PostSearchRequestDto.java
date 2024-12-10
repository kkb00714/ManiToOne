package com.finalproject.manitoone.domain.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSearchRequestDto {

  private String nickname;
  private String name;
  private String email;
  private String content;
  private Boolean isBlind;
}
