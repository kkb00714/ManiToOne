package com.finalproject.manitoone.domain;

import com.finalproject.manitoone.constants.NotiType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "notification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "noti_id", nullable = false)
  private Long notiId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private NotiType type;

  @Column(name = "related_object_id", nullable = false, columnDefinition = "bigint COMMENT '게시글 댓글, 팔로워 유저, 마니또 피드 ID'")
  private Long relatedObjectId;

  @Column(name = "is_read", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. 읽지 않음\\n1. 읽음'")
  @Builder.Default
  private Boolean isRead = false;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @ManyToOne
  @JoinColumn(name = "sender_user_id", nullable = false)
  private User senderUser;

  public void markAsRead() {
    this.isRead = true;
  }

  public void updateCreatedAt() {
    this.createdAt = LocalDateTime.now();
  }

  public void unMarkAsRead() {
    this.isRead = false;
  }
}
