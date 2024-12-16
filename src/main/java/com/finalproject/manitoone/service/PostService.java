package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.domain.AiPostLog;
import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.PostImage;
import com.finalproject.manitoone.domain.ReplyPost;
import com.finalproject.manitoone.domain.Report;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.UserPostLike;
import com.finalproject.manitoone.domain.dto.AddPostRequestDto;
import com.finalproject.manitoone.domain.dto.AddReportRequestDto;
import com.finalproject.manitoone.domain.dto.PostResponseDto;
import com.finalproject.manitoone.domain.dto.ReportResponseDto;
import com.finalproject.manitoone.domain.dto.UpdatePostRequestDto;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.dto.postimage.PostImageResponseDto;
import com.finalproject.manitoone.dto.replypost.ReplyPostResponseDto;
import com.finalproject.manitoone.repository.AiPostLogRepository;
import com.finalproject.manitoone.repository.ManitoLetterRepository;
import com.finalproject.manitoone.repository.ManitoMatchesRepository;
import com.finalproject.manitoone.repository.NotificationRepository;
import com.finalproject.manitoone.repository.PostImageRepository;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.ReplyPostRepository;
import com.finalproject.manitoone.repository.ReportRepository;
import com.finalproject.manitoone.repository.UserPostLikeRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.AlanUtil;
import com.finalproject.manitoone.util.NotificationUtil;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;
  private final UserPostLikeRepository userPostLikeRepository;
  private final ReplyPostRepository replyPostRepository;
  private final ManitoLetterRepository manitoLetterRepository;
  private final ReportRepository reportRepository;
  private final AiPostLogRepository aiPostLogRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  // ManitoMatchesRepository 생성으로 인한 필드 추가
  private final ManitoMatchesRepository manitoMatchesRepository;
  private final NotificationRepository notificationRepository;

  private final NotificationUtil notificationUtil;


  // 게시글 생성 (미완성)
  // TODO: 이미지 업로드
  @Async
  public PostResponseDto createPost(AddPostRequestDto request, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.save(Post.builder()
        .content(request.getContent())
        .user(user)
        .isManito(request.getIsManito())
        .build());

    AiPostLog aiPost = new AiPostLog(null, post, AlanUtil.getAlanAnswer(request.getContent()));
    aiPostLogRepository.save(aiPost);

    return new PostResponseDto(post.getPostId(), post.getUser(), post.getContent(),
        post.getCreatedAt(), post.getUpdatedAt(), post.getIsManito());
  }

  // 이미지 저장
//  private void saveImage(Post post, MultipartFile image) throws IOException {
//    // 파일 저장 경로 지정
//    String uploadDir = "src/main/resources/static/img/upload/";
//    Path uploadPath = Paths.get(uploadDir);
//    if (!Files.exists(uploadPath)) {
//      Files.createDirectories(uploadPath);
//    }
//
//    // 이미지 저장
//    String originalFilename = image.getOriginalFilename();
//    String uniqueFileName = UUID.randomUUID() + "-" + originalFilename;
//    Path filePath = uploadPath.resolve(uniqueFileName);
//    Files.write(filePath, image.getBytes());
//
//    postImageRepository.save(PostImage.builder()
//        .fileName(originalFilename)
//        .post(post)
//        .build());
//  }

  // 게시글 수정
  // TODO: 이미지 수정 
  public PostResponseDto updatePost(Long postId, UpdatePostRequestDto request, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    if (!post.getUser().getUserId().equals(user.getUserId())) {
      throw new IllegalArgumentException(IllegalActionMessages.DIFFERENT_USER.getMessage());
    }

    post.updatePost(request.getContent());
    post.changeUpdatedDate(LocalDateTime.now());

    Post updatedPost = postRepository.save(post);

    return PostResponseDto.builder()
        .postId(updatedPost.getPostId())
        .user(updatedPost.getUser())
        .content(updatedPost.getContent())
        .createdAt(updatedPost.getCreatedAt())
        .updatedAt(updatedPost.getUpdatedAt())
        .isManito(updatedPost.getIsManito())
        .build();
  }

  // 전체 게시글 조회
  public Page<PostResponseDto> getPosts(Pageable pageable) {
    Page<Post> posts = postRepository.findAll(pageable);

    if (posts.isEmpty()) {
      throw new IllegalArgumentException(IllegalActionMessages.CANNOT_FIND_ANY_POST.getMessage());
    }

    return posts.map(post -> new PostResponseDto(
        post.getPostId(),
        post.getUser(),
        post.getContent(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getIsManito()
    ));
  }

  // 게시글 상세 조회
  // TODO: 이미지 조회
  public PostResponseDto getPostDetail(Long postId) {
    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()
        ));

    return new PostResponseDto(
        post.getPostId(),
        post.getUser(),
        post.getContent(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getIsManito()
    );
  }

  // 게시글 삭제
  public void deletePost(Long postId) {
    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    deleteImages(postId);
    deleteReplies(postId);
    deleteLikes(postId);
    deleteManitoLetters(postId);
    postRepository.delete(post);
  }

  // 게시글 이미지 삭제
  private void deleteImages(Long postId) {
    List<PostImage> imageList = postImageRepository.findAllByPostPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_IMAGE_WITH_GIVEN_ID.getMessage()
        ));

    postImageRepository.deleteAll(imageList);
  }

  // 게시글 답글 삭제
  private void deleteReplies(Long postId) {
    List<ReplyPost> replyList = replyPostRepository.findAllByPostPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    replyPostRepository.deleteAll(replyList);
  }

  // 게시글 좋아요 삭제
  private void deleteLikes(Long postId) {
    List<UserPostLike> likeList = userPostLikeRepository.findAllByPostPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_POST_LIKE_WITH_GIVEN_ID.getMessage()
        ));

    userPostLikeRepository.deleteAll(likeList);
  }

//  // 마니또 편지 삭제
//  private void deleteManitoLetters(Long postId) {
//    List<ManitoLetter> manitoLetterList = manitoLetterRepository.findAllByPostIdPostId(postId)
//        .orElseThrow(() -> new IllegalArgumentException(
//            IllegalActionMessages.CANNOT_FIND_MANITO_LETTER_WITH_GIVEN_ID.getMessage()
//        ));
//
//    manitoLetterRepository.deleteAll(manitoLetterList);
//  }

  // 마니또 편지 삭제 (수정된 버전)
  private void deleteManitoLetters(Long postId) {
    // 해당 postId와 연결된 모든 매칭 찾기
    List<ManitoMatches> matches = manitoMatchesRepository.findAllByMatchedPostId_PostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_MANITO_MATCH_WITH_GIVEN_ID.getMessage()
        ));

    // 각 매칭에 연결된 편지들 찾아서 삭제
    matches.forEach(match -> manitoLetterRepository.findByManitoMatches_ManitoMatchesId(
            match.getManitoMatchesId())
        .ifPresent(manitoLetterRepository::delete));
  }

  // 게시글 숨기기
  public void hidePost(Long postId) {
    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    post.hidePost(true);

    postRepository.save(post);
  }

  // 게시글 좋아요
  public void likePost(Long postId, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()
        ));

    userPostLikeRepository.save(UserPostLike.builder()
        .post(post)
        .user(user)
        .build());

    try {
      Notification notification = notificationRepository.findByUserAndSenderUserAndTypeAndRelatedObjectId(post.getUser(), user,
          NotiType.LIKE_CLOVER, postId);
      // 이미 해당 게시글에 좋아요를 눌렀다면
      if (notification != null) {
        notification.updateCreatedAt();
        notification.unMarkAsRead();
        notificationRepository.save(notification);
        notificationUtil.sendAlarm(post.getUser());
      } else {
        notificationUtil.createNotification(post.getUser().getNickname(), user, NotiType.LIKE_CLOVER,
            postId);
      }

    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  // 게시글 신고
  public ReportResponseDto reportPost(Long postId, AddReportRequestDto request, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()
        ));

    Report report = reportRepository.save(Report.builder()
        .reportType(request.getReportType())
        .userId(user.getUserId())
        .type(ReportObjectType.POST)
        .reportObjectId(post.getPostId())
        .build());

    return ReportResponseDto.builder()
        .reportId(report.getReportId())
        .userId(report.getUserId())
        .reportObjectId(report.getReportObjectId())
        .reportType(report.getReportType())
        .type(report.getType())
        .build();
  }

  // 게시글 좋아요 개수 조회
  public Integer getPostLikesNum(Long postId) {
    List<UserPostLike> likes = userPostLikeRepository.findAllByPostPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_POST_LIKE_WITH_GIVEN_ID.getMessage()
        ));

    return likes.size();
  }

  public List<PostViewResponseDto> getPostsByNickName(String nickName, Pageable pageable) {
    List<Post> posts = postRepository.findAllByIsBlindFalseAndIsHiddenFalseAndUser_Nickname(
            nickName,
            pageable)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    List<PostViewResponseDto> postResponses = posts.stream()
        .map(post -> new PostViewResponseDto(post.getPostId(), post.getUser().getProfileImage(),
            post.getUser().getNickname(), post.getContent(), post.getCreatedAt(),
            post.getUpdatedAt()
        ))
        .toList();

    return addAdditionalDataToDto(postResponses);
  }

  public List<PostViewResponseDto> getLikePostByNickName(String nickName, Pageable pageable) {
    List<PostViewResponseDto> postResponses = userPostLikeRepository.findAllByUser_nicknameAndPost_IsHiddenFalseAndPost_IsBlindFalse(
            nickName, pageable)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()))
        .stream()
        .map(userPostLike -> new PostViewResponseDto(
            userPostLike.getPost().getPostId(),
            userPostLike.getUser().getProfileImage(),
            userPostLike.getUser().getNickname(),
            userPostLike.getPost().getContent(),
            userPostLike.getPost().getCreatedAt(),
            userPostLike.getPost().getUpdatedAt())
        ).toList();

    return addAdditionalDataToDto(postResponses);
  }

  public List<PostViewResponseDto> getMyHiddenPosts(String nickName, Pageable pageable) {
    List<PostViewResponseDto> postResponses = postRepository.findAllByIsBlindFalseAndIsHiddenTrueAndUser_Nickname(
            nickName, pageable)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()))
        .stream()
        .map(post -> new PostViewResponseDto(
            post.getPostId(),
            post.getUser().getProfileImage(),
            post.getUser().getNickname(),
            post.getContent(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        ))
        .toList();

    return addAdditionalDataToDto(postResponses);
  }

  private List<PostViewResponseDto> addAdditionalDataToDto(
      List<PostViewResponseDto> postResponses) {
    postResponses.forEach(postResponseDto -> {
      List<PostImageResponseDto> postImages = postImageRepository.findAllByPost_PostId(
              postResponseDto.getPostId())
          .orElseThrow(() -> new IllegalArgumentException(
              IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()))
          .stream()
          .map(postImage -> new PostImageResponseDto(postImage.getFileName()))  // 변환
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

  // 타임라인 조회를 위한 메서드
  public Page<PostViewResponseDto> getTimelinePosts(String nickname, Pageable pageable) {
    User currentUser = userService.getCurrentUser(nickname);

    Page<Post> posts = postRepository.findTimelinePostsByUserId(currentUser.getUserId(), pageable);
    return posts.map(post -> {
      PostViewResponseDto dto = new PostViewResponseDto(
          post.getPostId(),
          post.getUser().getProfileImage(),
          post.getUser().getNickname(),
          post.getContent(),
          post.getCreatedAt(),
          post.getUpdatedAt()
      );
      return addAdditionalDataToDto(List.of(dto)).get(0);
    });
  }

  // 단순 postId로 PostViewResponseDto를 가져오는 메서드 (마니또에서 씁니다)
  public PostViewResponseDto getPost(Long postId) {
    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    PostViewResponseDto postResponse = new PostViewResponseDto(
        post.getPostId(),
        post.getUser().getProfileImage(),
        post.getUser().getNickname(),
        post.getContent(),
        post.getCreatedAt(),
        post.getUpdatedAt()
    );

    // addAdditionalDataToDto 메서드를 활용하여 추가 데이터(이미지, 좋아요 수, 답글) 설정
    return addAdditionalDataToDto(List.of(postResponse)).get(0);
  }
}
