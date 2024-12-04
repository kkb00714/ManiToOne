package com.finalproject.manitoone.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reply_post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reply_post_id", nullable = false)
  private Long replyPostId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "content", nullable = false, columnDefinition = "text")
  private String content;

  @Column(name = "parrent_id")
  private Long parrentId;

  @Column(name = "is_blind", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. x\\n1. o\\n관리자가 숨길 수 있는 권한'")
  @Builder.Default
  private Boolean isBlind = false;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}