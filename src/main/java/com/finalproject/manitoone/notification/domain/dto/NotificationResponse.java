package com.finalproject.manitoone.notification.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finalproject.manitoone.notification.constants.NotiType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

  private Long notiId;
  private Long userId;
  private NotiType type;
  private String content;
  private Long relatedObjectId;
  private Boolean isRead = false;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt = LocalDateTime.now();
}
