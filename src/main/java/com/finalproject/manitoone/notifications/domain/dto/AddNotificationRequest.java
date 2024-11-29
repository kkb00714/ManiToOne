package com.finalproject.manitoone.notifications.domain.dto;

import com.finalproject.manitoone.notifications.constants.NotiType;
import com.finalproject.manitoone.notifications.domain.Notification;
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
