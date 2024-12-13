package com.finalproject.manitoone.dto.manito;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManitoLetterResponseDto {

  private Long manitoLetterId;
  private String letterContent;
  private String musicUrl;
  private String musicComment;
  private boolean isPublic;
  private boolean isReport;
  private boolean isAnswerReport;
  private String answerLetter;
  private String timeDiff;
  private boolean isOwner;
  private LocalDateTime createdAt;
  private String formattedCreatedAt;
}
