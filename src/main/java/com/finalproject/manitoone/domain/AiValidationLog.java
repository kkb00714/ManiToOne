package com.finalproject.manitoone.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_validation_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiValidationLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="validation_log_id", nullable = false)
  private Long validationLogId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Column(name = "validation_result", nullable = false)
  private boolean validationResult;

  @Column(name = "raw_ai_response", nullable = false, columnDefinition = "text")
  private String rawAiResponse;

  @Column(name="created_at", nullable = false)
  private LocalDateTime  createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
