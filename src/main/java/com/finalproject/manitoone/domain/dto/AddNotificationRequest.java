package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddNotificationRequest {
  private User user;
  private NotiType type;
  private Long relatedObjectId;

  public Notification toEntity() {
    return Notification.builder()
        .user(this.user)
        .type(this.type)
        .content(this.type.getMessage(this.user.getNickname()))
        .relatedObjectId(this.relatedObjectId)
        .build();
  }
}
