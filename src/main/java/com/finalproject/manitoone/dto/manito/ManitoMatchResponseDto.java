package com.finalproject.manitoone.dto.manito;

import com.finalproject.manitoone.constants.MatchStatus;
import com.finalproject.manitoone.domain.ManitoMatches;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManitoMatchResponseDto {
  private Long manitoMatchesId;
  private Long postId;
  private String postContent;
  private String userNickname;
  private LocalDateTime matchedTime;
  private MatchStatus status;

  public static ManitoMatchResponseDto from(ManitoMatches match) {
    return ManitoMatchResponseDto.builder()
        .manitoMatchesId(match.getManitoMatchesId())
        .postId(match.getMatchedPostId().getPostId())
        .postContent(match.getMatchedPostId().getContent())
        .userNickname(match.getMatchedUserId().getNickname())
        .matchedTime(match.getMatchedTime())
        .status(match.getStatus())
        .build();
  }

}
