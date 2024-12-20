package com.finalproject.manitoone.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPostRequestDto {

  private String content;
  private Boolean isManito;
  MultipartFile[] images;
}
