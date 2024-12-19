package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.ReportType;
import com.finalproject.manitoone.domain.AiPostLog;
import com.finalproject.manitoone.domain.AiValidationLog;
import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.PostImage;
import com.finalproject.manitoone.domain.ReplyPost;
import com.finalproject.manitoone.domain.Report;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.UserPostLike;
import com.finalproject.manitoone.domain.dto.AddPostRequestDto;
import com.finalproject.manitoone.domain.dto.AiFeedbackDto;
import com.finalproject.manitoone.domain.dto.PostResponseDto;
import com.finalproject.manitoone.domain.dto.ReportResponseDto;
import com.finalproject.manitoone.domain.dto.UpdatePostRequestDto;
import com.finalproject.manitoone.dto.post.PostViewResponseDto;
import com.finalproject.manitoone.dto.postimage.PostImageResponseDto;
import com.finalproject.manitoone.dto.replypost.ReplyPostResponseDto;
import com.finalproject.manitoone.repository.AiPostLogRepository;
import com.finalproject.manitoone.repository.AiValidationLogRepository;
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
import com.finalproject.manitoone.util.FileUtil;
import jakarta.transaction.Transactional;
import java.nio.file.Paths;
import com.finalproject.manitoone.util.NotificationUtil;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
  private final NotificationRepository notificationRepository;
  private final ManitoMatchesRepository manitoMatchesRepository;
  private final AiValidationLogRepository aiValidationLogRepository;
  private final S3Service s3Service;
  private final FileUtil fileUtil;
  private final NotificationUtil notificationUtil;

  // 게시글 생성
  @Transactional
  public PostResponseDto createPost(AddPostRequestDto request, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.save(Post.builder()
        .content(request.getContent())
        .user(user)
        .isManito(request.getIsManito())
        .build());

    if (request.getImages() != null) {
      try {
        for (MultipartFile image : request.getImages()) {
          String fileName = s3Service.uploadImage(image);
          postImageRepository.save(PostImage.builder()
              .post(post)
              .fileName(fileName)
              .build());
        }
      } catch (IOException e) {
        throw new IllegalArgumentException(
            IllegalActionMessages.CANNOT_SAVE_IMAGE.getMessage() + ": " + e.getMessage());
      }
    }

    return PostResponseDto.builder()
        .postId(post.getPostId())
        .content(post.getContent())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .isManito(post.getIsManito())
        .build();
  }

  // AI 피드백
  @Async
  public AiFeedbackDto getFeedback(String content) {
    String feedback = AlanUtil.getValidationAnswer(content);
    return AiFeedbackDto.builder()
        .feedback(feedback)
        .build();
  }

  // 게시글 수정
  @Transactional
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

    int uploadedImagesNum = getImages(post.getPostId()).size();
    int toUploadImagesNum = request.getImages().length;

    if (toUploadImagesNum > 4 || (uploadedImagesNum + toUploadImagesNum) > 4) {
      throw new IllegalArgumentException(IllegalActionMessages.CANNOT_SAVE_IMAGE.getMessage());
    }

    if (request.getImages() != null) {
      try {
        for (MultipartFile image : request.getImages()) {
          String fileName = s3Service.uploadImage(image);
          postImageRepository.save(PostImage.builder()
              .post(post)
              .fileName(fileName)
              .build());
        }
      } catch (IOException e) {
        throw new IllegalArgumentException(
            IllegalActionMessages.CANNOT_SAVE_IMAGE.getMessage() + ": " + e.getMessage());
      }
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
    Page<Post> posts = postRepository.findAllByIsHiddenFalseAndIsBlindFalse(pageable)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_ANY_POST.getMessage()
        ));

    return posts.map(post -> new PostResponseDto(
        post.getPostId(),
        post.getUser(),
        post.getContent(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getIsManito(),
        getPostLikesNum(post.getPostId())
    ));
  }

  // 게시글 상세 조회
  public PostResponseDto getPostDetail(Long postId) {
    Post post = postRepository.findByPostIdAndIsHiddenFalseAndIsBlindFalse(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()
        ));

    return new PostResponseDto(
        post.getPostId(),
        post.getUser(),
        post.getContent(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getIsManito(),
        getPostLikesNum(post.getPostId())
    );
  }

  // 게시글 이미지 조회
  public List<PostImageResponseDto> getImages(Long postId) {
    List<PostImage> images = postImageRepository.findAllByPostPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_IMAGE_WITH_GIVEN_ID.getMessage()
        ));

    return images.stream().map(image -> new PostImageResponseDto(
        image.getFileName())).toList();
  }

  // 게시글 삭제
  public void deletePost(Long postId, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    if (!user.equals(post.getUser())) {
      throw new IllegalArgumentException(
          IllegalActionMessages.CANNOT_DELETE_POST_AND_REPLY.getMessage());
    }

    deleteImages(postId);
    deleteReplies(postId);
    deleteLikes(postId);
    deleteReports(postId);
    deleteNotis(postId);
    deleteManitoLetters(postId);
    deleteAiPostLogs(postId);
    deleteAiValidationLogs(postId);
    postRepository.delete(post);
  }

  // 게시글 이미지 삭제
  private void deleteImages(Long postId) {
    List<PostImage> postImages = postImageRepository.findAllByPostPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_IMAGE_WITH_GIVEN_ID.getMessage()
        ));

    postImageRepository.deleteAll(postImages);
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
    List<UserPostLike> postLikes = userPostLikeRepository.findAllByPostPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_POST_LIKE_WITH_GIVEN_ID.getMessage()
        ));

    userPostLikeRepository.deleteAll(postLikes);
  }

  // 마니또 편지 삭제
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

  // 게시글 신고 목록 삭제
  private void deleteReports(Long postId) {
    List<Report> reports = reportRepository.findAllByTypeAndReportObjectId(ReportObjectType.POST,
            postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPORT_WITH_GIVEN_ID.getMessage()
        ));

    reportRepository.deleteAll(reports);
  }

  // 알림 삭제
  private void deleteNotis(Long postId) {
    List<Notification> notis = notificationRepository.findByTypeInAndRelatedObjectId(List.of(
            NotiType.LIKE_CLOVER, NotiType.POST_REPLY, NotiType.POST_RE_REPLY), postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_ANY_NOTIFICATIONS.getMessage()
        ));

    notificationRepository.deleteAll(notis);
  }

  // AI 포스트 요약 삭제
  private void deleteAiPostLogs(Long postId) {
    List<AiPostLog> logs = aiPostLogRepository.findAllByPostPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_AI_POST_LOG.getMessage()
        ));

    aiPostLogRepository.deleteAll(logs);
  }

  // AI Validation Log 삭제
  private void deleteAiValidationLogs(Long postId) {
    List<AiValidationLog> logs = aiValidationLogRepository.findAllByPostPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_AI_POST_LOG.getMessage()
        ));

    aiValidationLogRepository.deleteAll(logs);
  }

  // 게시글 숨기기
  public void hidePost(Long postId, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    post.hidePost(!Boolean.TRUE.equals(post.getIsHidden()));

    if (!user.equals(post.getUser())) {
      throw new IllegalArgumentException(IllegalActionMessages.CANNOT_HIDE_POST.getMessage());
    }

    postRepository.save(post);
  }

  // 게시글 좋아요
  public PostResponseDto likePost(Long postId, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.findByPostIdAndIsHiddenFalseAndIsBlindFalse(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()
        ));

    Optional<UserPostLike> existingLike = userPostLikeRepository.findByUser_UserIdAndPost_PostId(
        user.getUserId(), post.getPostId());

    if (existingLike.isPresent()) {
      userPostLikeRepository.delete(existingLike.get());
    } else {
      userPostLikeRepository.save(UserPostLike.builder()
          .post(post)
          .user(user)
          .build());
    }

    try {
      Notification notification = notificationRepository.findByUserAndSenderUserAndTypeAndRelatedObjectId(
          post.getUser(), user,
          NotiType.LIKE_CLOVER, postId);
      // 이미 해당 게시글에 좋아요를 눌렀다면
      if (notification != null) {
        notification.updateCreatedAt();
        notification.unMarkAsRead();
        notificationRepository.save(notification);
        notificationUtil.sendAlarm(post.getUser());
      } else {
        notificationUtil.createNotification(post.getUser().getNickname(), user,
            NotiType.LIKE_CLOVER,
            postId);
      }
    } catch (IOException e) {
      log.error(e.getMessage());
    }

    return PostResponseDto.builder()
        .postId(post.getPostId())
        .user(post.getUser())
        .likesNumber(getPostLikesNum(post.getPostId()))
        .build();
  }

  // 게시글 신고
  public ReportResponseDto reportPost(Long postId, String reportType, String email) {
    ReportType theType = null;

    for (ReportType type : ReportType.values()) {
      if (type.name().equals(reportType)) {
        theType = type;
        break;
      }
    }

    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.findByPostIdAndIsHiddenFalseAndIsBlindFalse(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()
        ));

    Report report = reportRepository.save(Report.builder()
        .reportType(theType)
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
    List<UserPostLike> likes = userPostLikeRepository.findAllByPostPostIdAndReplyPostIdNull(postId)
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
            userPostLike.getPost().getUser().getNickname(),
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

    return addAdditionalDataToDto(List.of(postResponse)).get(0);
  }
}
