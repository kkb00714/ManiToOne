package com.finalproject.manitoone.dto.replypost;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplyPostResponseDto {

  private String nickName;
  private String profileImage;
  private String content;
  private LocalDateTime createdAt;
}
