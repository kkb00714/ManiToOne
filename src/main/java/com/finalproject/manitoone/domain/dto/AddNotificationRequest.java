package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddNotificationRequest {

//  private User receiveUser;
//  private User sendUser;
  private NotiType type;
  private Long relatedObjectId;

  public Notification toEntity() {
    return Notification.builder()
//        .user(this.receiveUser)
        .type(this.type)
        .relatedObjectId(this.relatedObjectId)
        .build();
  }
}
