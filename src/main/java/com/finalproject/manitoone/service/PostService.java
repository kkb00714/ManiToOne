package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.dto.post.PostResponseDto;
import com.finalproject.manitoone.dto.postimage.PostImageResponseDto;
import com.finalproject.manitoone.repository.PostImageRepository;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.UserPostLikeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;
  private final UserPostLikeRepository userPostLikeRepository;

  public List<PostResponseDto> getPostsByNickName(String nickName, Pageable pageable) {
    // TODO: 내 게시글인지는 어떻게 판별할까요?
    List<Post> posts = postRepository.findAllByIsBlindFalseAndIsHiddenFalseAndUser_Nickname(
            nickName,
            pageable)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 유저 ID를 찾을 수 없습니다."));

    List<PostResponseDto> postResponses = posts.stream()
        .map(Post::toPostResponseDto)
        .toList();

    return addAdditionalDataToDto(postResponses);
  }

  public List<PostResponseDto> getLikePostByNickName(String nickName, Pageable pageable) {
    List<PostResponseDto> postResponses = userPostLikeRepository.findAllByUser_nicknameAndPost_IsHiddenFalseAndPost_IsBlindFalse(
            nickName, pageable)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 유저 ID를 찾을 수 없습니다."))
        .stream()
        .map(userPostLike -> new PostResponseDto(
            userPostLike.getPost().getPostId(),
            userPostLike.getUser().getUserId(),
            userPostLike.getPost().getContent(),
            userPostLike.getPost().getCreatedAt(),
            userPostLike.getPost().getUpdatedAt(),
            null,
            null)
        ).toList();

    return addAdditionalDataToDto(postResponses);
  }

  public List<PostResponseDto> getMyHiddenPosts(String nickName, Pageable pageable) {
    List<PostResponseDto> postResponses = postRepository.findAllByIsBlindFalseAndIsHiddenTrueAndUser_Nickname(
            nickName, pageable)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 유저 ID를 찾을 수 없습니다."))
        .stream()
        .map(Post::toPostResponseDto)
        .toList();

    return addAdditionalDataToDto(postResponses);
  }

  private List<PostResponseDto> addAdditionalDataToDto(List<PostResponseDto> postResponses) {
    postResponses.forEach(postResponseDto -> {
      List<PostImageResponseDto> postImages = postImageRepository.findAllByPost_PostId(
              postResponseDto.getPostId())
          .orElseThrow(() -> new IllegalArgumentException("해당하는 포스트 ID를 찾을 수 없습니다."))
          .stream()
          .map(postImage -> new PostImageResponseDto(postImage.getFileName()))  // 변환
          .toList();
      Integer likeCount = userPostLikeRepository.countAllByPost_PostId(postResponseDto.getPostId())
          .orElseThrow(() -> new IllegalArgumentException("해당하는 포스트 ID를 찾을 수 없습니다."));
      postResponseDto.addLikeCount(likeCount);
      postResponseDto.addPostImages(postImages);
    });

    return postResponses;
  }
}
