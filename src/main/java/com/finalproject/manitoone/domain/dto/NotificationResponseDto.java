package com.finalproject.manitoone.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.dto.user.UserInformationResponseDto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {

  private Long notiId;
  private UserInformationResponseDto user;
  private NotiType type;
  @Setter
  private String content;
  private String nickname;
  private Long relatedObjectId;
  private Boolean isRead;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;
  private String timeDifference;
}
