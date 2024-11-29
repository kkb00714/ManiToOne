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
  private Integer status;
  private List<UserInformationResponseDto> followers;
  private List<UserInformationResponseDto> followings;

  public void setFollow(List<UserInformationResponseDto> followers,
      List<UserInformationResponseDto> followings) {
    this.followers = followers;
    this.followings = followings;
  }
}