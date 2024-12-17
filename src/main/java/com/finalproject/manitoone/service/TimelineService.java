package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.dto.postimage.PostImageResponseDto;
import com.finalproject.manitoone.dto.replypost.ReplyPostResponseDto;
import com.finalproject.manitoone.repository.PostImageRepository;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.ReplyPostRepository;
import com.finalproject.manitoone.repository.UserPostLikeRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimelineService {
  private static final double FOLLOWING_RATIO = 0.8;
  private final PostRepository postRepository;
  private final UserService userService;
  private final PostImageRepository postImageRepository;
  private final UserPostLikeRepository userPostLikeRepository;
  private final ReplyPostRepository replyPostRepository;

  @Transactional(readOnly = true)
  public Page<PostViewResponseDto> getTimelinePosts(String nickname, Pageable pageable, int recentDays) {
    User currentUser = userService.getCurrentUser(nickname);
    int pageSize = pageable.getPageSize();

    // 팔로우 게시물 개수 계산
    int followingPostsCount = (int) (pageSize * FOLLOWING_RATIO);
    int randomPostsCount = pageSize - followingPostsCount;

    // 팔로우 게시물 조회
    Page<Post> followingPosts = postRepository.findTimelinePostsByUserId(
        currentUser.getUserId(),
        PageRequest.of(pageable.getPageNumber(), followingPostsCount, pageable.getSort())
    );

    // 랜덤 게시물 조회 (최적화된 쿼리)
    List<Post> randomPosts = postRepository.findRandomRecentPosts(
        currentUser.getUserId(),
        LocalDateTime.now().minusDays(recentDays),
        randomPostsCount,
        followingPosts.getContent().stream().map(Post::getPostId).collect(Collectors.toList())
    );

    return mergeAndPaginatePosts(followingPosts, randomPosts, pageable);
  }

  private Page<PostViewResponseDto> mergeAndPaginatePosts(
      Page<Post> followingPosts,
      List<Post> randomPosts,
      Pageable pageable) {
    List<Post> mergedPosts = Stream.concat(
            followingPosts.getContent().stream(),
            randomPosts.stream())
        .distinct()
        .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
        .collect(Collectors.toList());

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), mergedPosts.size());

    if (start >= mergedPosts.size()) {
      return Page.empty(pageable);
    }

    List<PostViewResponseDto> content = mergedPosts.subList(start, end)
        .stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());

    return new PageImpl<>(content, pageable, mergedPosts.size());
  }

  private PostViewResponseDto convertToDto(Post post) {
    PostViewResponseDto dto = new PostViewResponseDto(
        post.getPostId(),
        post.getUser().getProfileImage(),
        post.getUser().getNickname(),
        post.getContent(),
        post.getCreatedAt(),
        post.getUpdatedAt()
    );
    return addAdditionalDataToDto(List.of(dto)).get(0);
  }

  // 추가 데이터(이미지, 좋아요, 답글) 처리 메소드
  private List<PostViewResponseDto> addAdditionalDataToDto(List<PostViewResponseDto> postResponses) {
    postResponses.forEach(postResponseDto -> {
      List<PostImageResponseDto> postImages = postImageRepository.findAllByPost_PostId(
              postResponseDto.getPostId())
          .orElseThrow(() -> new IllegalArgumentException(
              IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()))
          .stream()
          .map(postImage -> new PostImageResponseDto(postImage.getFileName()))
          .toList();
      Integer likeCount = userPostLikeRepository.countAllByPost_PostId(postResponseDto.getPostId())
          .orElseThrow(() -> new IllegalArgumentException(
              IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));
      List<ReplyPostResponseDto> replies = replyPostRepository.findAllByPost_PostIdAndIsBlindFalse(
              postResponseDto.getPostId()).orElseThrow(() -> new IllegalArgumentException(
              IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()))
          .stream()
          .map(reply -> new ReplyPostResponseDto(reply.getUser().getNickname(),
              reply.getUser().getProfileImage(), reply.getContent(), reply.getCreatedAt()))
          .toList();
      postResponseDto.addLikeCount(likeCount);
      postResponseDto.addPostImages(postImages);
      postResponseDto.addReplies(replies);
    });

    return postResponses;
  }
}
