package com.finalproject.manitoone.dto.manito;

import com.finalproject.manitoone.util.ManitoCommentParser;
import com.finalproject.manitoone.domain.ManitoComment;
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
public class ManitoCommentRequestDto {

  @NotNull
  @Size(max = 600)
  private String content;

  @Size(max = 200)
  private String musicUrl;

  @Size(max = 100)
  private String musicComment;

  public ManitoComment toEntity(Post post, User user) {
    return ManitoComment.builder()
        .postId(post)
        .user(user)
        .comment(ManitoCommentParser.combineComment(content, musicUrl, musicComment))
        .build();
  }
}
