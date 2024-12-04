package com.finalproject.manitoone.dto.manito;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManitoCommentResponseDto {

  private Long manitoCommentId;
  private String content;
  private String musicUrl;
  private String musicComment;
  private boolean isPublic;
  private boolean isReport;
  private String answerComment;
  private String timeDiff;
  private boolean isOwner;
}
