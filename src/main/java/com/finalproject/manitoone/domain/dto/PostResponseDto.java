package com.finalproject.manitoone.domain.dto;

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
public class PostResponseDto {

  private Long postId;
  private User user;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdDiff;
  private String updatedDiff;
  private Boolean isManito;
  private Integer likesNumber;
}
