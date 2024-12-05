package com.finalproject.manitoone.domain.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchRequestDto {

  private String nickname;
  private String email;
  private Integer status;
}
