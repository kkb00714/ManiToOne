package com.finalproject.manitoone.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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

  @JsonProperty("content")
  private String content;

  @JsonProperty("isManito")
  private Boolean isManito;
  // List<MultipartFile> images;
}
