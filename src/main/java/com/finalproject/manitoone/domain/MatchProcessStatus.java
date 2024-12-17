package com.finalproject.manitoone.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_process_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MatchProcessStatus {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="process_id", nullable = false)
  private Long id;

  @Column(name="nickname", nullable = false)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(name="status", nullable = false)
  private ProcessStatus status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "timeout_at", nullable = false)
  private LocalDateTime timeoutAt;

  public enum ProcessStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED
  }

  public void complete() {
    this.status = ProcessStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
  }

  public void fail() {
    this.status = ProcessStatus.FAILED;
    this.completedAt = LocalDateTime.now();
  }

  public boolean isTimedOut() {
    return LocalDateTime.now().isAfter(timeoutAt);
  }

  public static MatchProcessStatus create(String nickname) {
    return MatchProcessStatus.builder()
        .nickname(nickname)
        .status(ProcessStatus.IN_PROGRESS)
        .createdAt(LocalDateTime.now())
        .timeoutAt(LocalDateTime.now().plusSeconds(30))
        .build();
  }
}
