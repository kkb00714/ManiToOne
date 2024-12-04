package com.finalproject.manitoone.dto.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.ReplyPost;
import com.finalproject.manitoone.dto.postimage.PostImageResponseDto;
import com.finalproject.manitoone.dto.replypost.ReplyPostResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostViewResponseDto {

  private Long postId;
  private String profileImage;
  private String nickName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String content;
  private List<PostImageResponseDto> postImages;
  private Integer likeCount;
  private List<ReplyPostResponseDto> replies;

  public void addPostImages(List<PostImageResponseDto> postImages) {
    this.postImages = postImages;
  }

  public void addLikeCount(Integer likeCount) {
    this.likeCount = likeCount;
  }

  public void addReplies(List<ReplyPostResponseDto> replies) {
    this.replies = replies;
  }

  public PostViewResponseDto(Post post) {
    this.postId = post.getPostId();
    this.profileImage = post.getUser().getProfileImage();
    this.nickName = post.getUser().getNickname();
    this.content = post.getContent();
    this.createdAt = post.getCreatedAt();
    this.updatedAt = post.getUpdatedAt();
    this.postImages = null;
    this.likeCount = null;
    this.replies = null;
  }
}
