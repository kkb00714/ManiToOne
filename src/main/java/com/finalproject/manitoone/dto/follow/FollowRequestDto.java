package com.finalproject.manitoone.dto.follow;

import com.finalproject.manitoone.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowRequestDto {

  private User follower;
  private User following;
}
