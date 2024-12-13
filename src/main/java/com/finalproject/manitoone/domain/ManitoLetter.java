package com.finalproject.manitoone.domain;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import com.finalproject.manitoone.constants.MatchStatus;
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
  @JoinColumn(name = "manito_matches_id", nullable = false)
  private ManitoMatches manitoMatches;

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


  // 편지 쓴 사람
  public User getLetterWriter() {
    return this.manitoMatches.getMatchedUserId();
  }

  // 편지 받은 사람
  public User getLetterReceiver() {
    return this.manitoMatches.getMatchedPostId().getUser();
  }

  // 답장 쓰기
  public void addAnswer(String answerComment, String userNickname) {
    if (!getLetterReceiver().getNickname().equals(userNickname)) {
      throw new IllegalStateException(ManitoErrorMessages.NO_PERMISSION_REPLY.getMessage());
    }
    if (this.answerLetter != null) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_ANSWERED.getMessage());
    }
    this.answerLetter = answerComment;
  }

  // 편지 신고
  public void reportLetter(String userNickname) {
    if (isReport) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_REPORTED.getMessage());
    }
    if (getLetterWriter().getNickname().equals(userNickname)) {
      throw new IllegalStateException(ManitoErrorMessages.OWN_LETTER_REPORT.getMessage());
    }
    this.isReport = true;
    this.manitoMatches.markAsReported();
  }

  // 답장 신고
  public void reportAnswer(String userNickname) {
    if (this.answerLetter == null) {
      throw new IllegalStateException(ManitoErrorMessages.ANSWER_NOT_FOUND.getMessage());
    }
    if (isAnswerReport) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_REPORTED_ANSWER.getMessage());
    }
    if (getLetterReceiver().getNickname().equals(userNickname)) {
      throw new IllegalStateException(ManitoErrorMessages.OWN_ANSWER_REPORT.getMessage());
    }
    this.isAnswerReport = true;
  }

  // 공개 토글
  public void toggleVisibility(String userNickname) {
    if (!getLetterReceiver().getNickname().equals(userNickname)) {
      throw new IllegalStateException(ManitoErrorMessages.NO_PERMISSION_VISIBILITY.getMessage());
    }
    this.isPublic = !this.isPublic;
  }

  // 편지 소유 확인
  public boolean isOwnedBy(User user) {
    return getLetterReceiver().equals(user);
  }

}

