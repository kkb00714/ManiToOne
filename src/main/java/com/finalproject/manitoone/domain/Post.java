package com.finalproject.manitoone.domain;

import com.finalproject.manitoone.dto.post.PostResponseDto;
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
@Table(name = "post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_id", nullable = false)
  private Long postId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "content", nullable = false, columnDefinition = "text")
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "is_manito", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. x\\n1. o'")
  @Builder.Default
  private Boolean isManito = false;

  @Column(name = "is_selected", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. x\\n1. o'")
  @Builder.Default
  private Boolean isSelected = false;

  @Column(name = "is_hidden", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. x\\n1. o\\n자신이 숨길 수 있는 권한'")
  @Builder.Default
  private Boolean isHidden = false;

  @Column(name = "is_blind", nullable = false, columnDefinition = "tinyint DEFAULT 0 COMMENT '0. x\\n1. o\\n관리자가 숨기는 권한'")
  @Builder.Default
  private Boolean isBlind = false;

  public PostResponseDto toPostResponseDto() {
    return new PostResponseDto(this.postId, this.user.getUserId(), this.content, this.createdAt, this.updatedAt,
        null, null);
  }
}