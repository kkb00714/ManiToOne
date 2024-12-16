package com.finalproject.manitoone.dto.admin;

import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.admin.PostSearchResponseDto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManitoSearchResponseDto {

  private Long manitoLetterId;
  private PostSearchResponseDto post;
  private User user;
  private String letterContent;
  private String musicUrl;
  private String musicComment;
  private boolean isReport = false;
  private boolean isPublic = false;
  private String answerLetter;
  private LocalDateTime createdAt = LocalDateTime.now();
  private boolean isAnswerReport = false;
}