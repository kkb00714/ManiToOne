package com.finalproject.manitoone.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.dto.user.UserInformationResponseDto;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {

  private Long notiId;
  private UserInformationResponseDto user;
  private NotiType type;
  private String content;
  private String nickname;
  private Long relatedObjectId;
  private Boolean isRead = false;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt = LocalDateTime.now();
  private String timeDifference;

  public NotificationResponseDto(Notification notification) {
    this.notiId = notification.getNotiId();
    this.type = notification.getType();
    this.relatedObjectId = notification.getRelatedObjectId();
    this.isRead = notification.getIsRead();
    this.createdAt = notification.getCreatedAt();
    setTimeDifference();
  }

  public void setTimeDifference() {
    LocalDateTime now = LocalDateTime.now();
    Duration duration = Duration.between(this.createdAt, now);

    if (duration.toMinutes() < 1) {
      this.timeDifference = "방금";
    } else if (duration.toHours() < 1) {
      this.timeDifference = duration.toMinutes() + "분";
    } else if (duration.toDays() < 1) {
      this.timeDifference = duration.toHours() + "시간";
    } else {
      this.timeDifference = duration.toDays() + "일";
    }
  }

  public void setContent(String nickname) {
    this.nickname = nickname;
    this.content = this.type.getMessage(nickname);
  }
}
