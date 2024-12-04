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
@Table(name = "manito_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ManitoComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "manito_comment_id", nullable = false)
  private Long manitoCommentId;

  @OneToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post postId;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "comment", columnDefinition = "text")
  private String comment;

  @Column(name = "is_report", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. 신고 안됨\\n1. 신고됨'")
  @Builder.Default
  private boolean isReport = false;

  @Column(name = "is_public", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. 공개 안함\\n1. 공개'")
  @Builder.Default
  private boolean isPublic = false;

  @Column(name = "answer_comment", columnDefinition = "text")
  private String answerComment;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  // 답장 신고 엔티티 추가함
  @Column(name = "is_answer_report")
  @Builder.Default
  private boolean isAnswerReport = false;

  // 답장
  public void addAnswer(String answerComment, String userNickname) {
    validateOwnership(userNickname);
    if (this.answerComment != null) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_ANSWERED.getMessage());
    }
    this.answerComment = answerComment;
  }

  // 신고 로직
  public void reportComment() {
    if (isReport) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_REPORTED.getMessage());
    }
    this.isReport = true;
  }

  public void reportAnswer(String userNickname) {
    validateOwnership(userNickname);
    if (this.answerComment == null) {
      throw new IllegalStateException(ManitoErrorMessages.ANSWER_NOT_FOUND.getMessage());
    }
    if (isAnswerReport) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_REPORTED_ANSWER.getMessage());
    }
    this.isAnswerReport = true;
  }

  // 공개 토글
  public void toggleVisibility(String userNickname) {
    validateOwnership(userNickname); // 본인 거 맞는지 확인하고
    this.isPublic = !this.isPublic; // 공개 설정 온오프
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

