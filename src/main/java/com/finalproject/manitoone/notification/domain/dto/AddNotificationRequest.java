package com.finalproject.manitoone.notification.domain.dto;

import com.finalproject.manitoone.notification.constants.NotiType;
import com.finalproject.manitoone.notification.domain.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddNotificationRequest {
  private Long userId;
  private NotiType type;
  private Long relatedObjectId;

  public Notification toEntity() {
    return Notification.builder()
        .userId(this.userId)
        .type(this.type)
        .content(this.type.getMessage(userId + ""))
        .relatedObjectId(this.relatedObjectId)
        .build();
  }
}