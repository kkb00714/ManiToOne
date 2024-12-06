package com.finalproject.manitoone.domain;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "manito_letter")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ManitoLetter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "manito_letter_id", nullable = false)
  private Long manitoLetterId;

  @OneToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post postId;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "letter-content", nullable = false)
  private String letterContent;

  @Column(name = "music-url")
  private String musicUrl;

  @Column(name = "music-comment")
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

  // 답장
  public void addAnswer(String answerComment, String userNickname) {
    validateOwnership(userNickname);
    if (this.answerLetter != null) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_ANSWERED.getMessage());
    }
    this.answerLetter = answerComment;
  }

  // 신고 로직
  public void reportLetter() {
    if (isReport) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_REPORTED.getMessage());
    }
    this.isReport = true;
  }

  public void reportAnswer(String userNickname) {
    validateOwnership(userNickname);
    if (this.answerLetter == null) {
      throw new IllegalStateException(ManitoErrorMessages.ANSWER_NOT_FOUND.getMessage());
    }
    if (isAnswerReport) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_REPORTED_ANSWER.getMessage());
    }
    this.isAnswerReport = true;
  }

  // 공개 토글
  public void toggleVisibility(String userNickname) {
    validateOwnership(userNickname);
    this.isPublic = !this.isPublic;
  }

  // 검증 로직
  public boolean isOwnedBy(User user) {
    return this.postId.getUser().equals(user);
  }

  public void validateOwnership(String userNickname) {
    if (!this.postId.getUser().getNickname().equals(userNickname)) {
      throw new IllegalStateException(ManitoErrorMessages.NO_PERMISSION_REPLY.getMessage());
    }
  }


}

