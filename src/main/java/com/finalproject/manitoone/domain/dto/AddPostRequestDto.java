package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.domain.Post;
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

  private String content;
  private Boolean isManito;
  List<MultipartFile> images;

  public Post toEntity() {
    return Post.builder()
        .content(this.content)
        .isManito(this.isManito)
        .build();
  }
}
