package com.finalproject.manitoone.dto.admin;

import com.finalproject.manitoone.domain.User;
import jakarta.persistence.Column;
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

  @Column(name = "letter_content", nullable = false)
  private String letterContent;

  @Column(name = "music_url")
  private String musicUrl;

  @Column(name = "music_comment")
  private String musicComment;

  @Column(name = "is_report", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. 신고 안됨\\n1. 신고됨'")
  @Builder.Default
  private boolean isReport = false;

  @Column(name = "is_public", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. 공개 안함\\n1. 공개'")
  @Builder.Default
  private boolean isPublic = false;

  @Column(name = "answer_letter", columnDefinition = "text")
  private String answerLetter;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "is_answer_report", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. 신고 안됨\\n1. 신고됨'")
  @Builder.Default
  private boolean isAnswerReport = false;
}
