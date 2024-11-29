package com.finalproject.manitoone.dto.follow;

import com.finalproject.manitoone.domain.Follow;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowResponseDto {

  private List<Follow> followers;
  private List<Follow> followings;
}
