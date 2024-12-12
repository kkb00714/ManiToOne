package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginResponseDto {

  private Long userId;
  private String email;
  private String name;
  private String nickname;
  private String profileImage;
  private String introduce;

  public UserLoginResponseDto(User user) {
    this.userId = user.getUserId();
    this.email = user.getEmail();
    this.name = user.getName();
    this.nickname = user.getNickname();
    this.profileImage = user.getProfileImage();
    this.introduce = user.getIntroduce();
  }
}
