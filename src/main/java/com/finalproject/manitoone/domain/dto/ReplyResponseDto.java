package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyResponseDto {
  private Post post;
  private User user;
  private Long parentId;
  private String content;
  private LocalDateTime createdAt;
}
