package com.finalproject.manitoone.dto.manito;

import com.finalproject.manitoone.util.ManitoLetterParser;
import com.finalproject.manitoone.domain.ManitoLetter;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ManitoLetterRequestDto {

  @NotNull
  @Size(max = 600)
  private String content;

  @Size(max = 200)
  private String musicUrl;

  @Size(max = 100)
  private String musicComment;

  public ManitoLetter toEntity(Post post, User user) {
    return ManitoLetter.builder()
        .postId(post)
        .user(user)
        .letter(ManitoLetterParser.combineLetter(content, musicUrl, musicComment))
        .build();
  }
}
