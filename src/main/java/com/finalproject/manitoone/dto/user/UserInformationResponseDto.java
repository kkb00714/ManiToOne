package com.finalproject.manitoone.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInformationResponseDto {

  private String name;
  private String nickname;
  private String introduce;
  private String profileImage;
  private List<UserInformationResponseDto> followings;
  private List<UserInformationResponseDto> followers;

  public void setFollow(List<UserInformationResponseDto> followings,
      List<UserInformationResponseDto> followers) {
    this.followings = followings;
    this.followers = followers;
  }

  public UserInformationResponseDto(String name, String nickname, String introduce,
      String profileImage) {
    this.name = name;
    this.nickname = nickname;
    this.introduce = introduce;
    this.profileImage = profileImage;
  }

  public UserInformationResponseDto(String nickname) {
    this.nickname = nickname;
  }
}
