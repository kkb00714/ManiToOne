package com.finalproject.manitoone.domain;


import com.finalproject.manitoone.constants.MatchStatus;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "manito_matches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ManitoMatches {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "manito_matches_id", nullable = false)
  private Long manitoMatchesId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post matchedPostId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User matchedUserId;

  @Column(name = "matched_time", nullable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
  @Builder.Default
  private LocalDateTime matchedTime = LocalDateTime.now();

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private MatchStatus status = MatchStatus.MATCHED;


  // 편지가 신고되었을 때
  public void markAsReported() {
    this.status = MatchStatus.REPORTED;
  }

  // 24시간이 지나 만료되었을 때
  public void markAsExpired() {
    this.status = MatchStatus.EXPIRED;
  }

  // 유저가 PASS를 선택했을 때
  public void markAsPassed() {
    if (this.status != MatchStatus.MATCHED) {
      throw new IllegalStateException("이미 처리된 매칭은 PASS할 수 없습니다.");
    }
    this.status = MatchStatus.PASSED;
  }
}
