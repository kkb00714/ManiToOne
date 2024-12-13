package com.finalproject.manitoone.domain;

import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.ReportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "report_id", nullable = false)
  private Long reportId;

  @Column(name = "report_type", nullable = false, length = 100)
  @Enumerated(EnumType.STRING)
  private ReportType reportType;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "type", nullable = false, length = 100, columnDefinition = "varchar(100) COMMENT '게시글 신고\\n댓글 신고\\n마니또 신고'")
  @Enumerated(EnumType.STRING)
  private ReportObjectType type;

  @Column(name = "report_object_id", nullable = false, columnDefinition = "int COMMENT '게시글, 댓글, 마니또 ID'")
  private Long reportObjectId;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}
