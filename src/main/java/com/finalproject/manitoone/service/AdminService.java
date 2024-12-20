package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.ReportType;
import com.finalproject.manitoone.constants.SearchType;
import com.finalproject.manitoone.domain.AiValidationLog;
import com.finalproject.manitoone.domain.ManitoLetter;
import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.Notification;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.PostImage;
import com.finalproject.manitoone.domain.QManitoLetter;
import com.finalproject.manitoone.domain.QManitoMatches;
import com.finalproject.manitoone.domain.QPost;
import com.finalproject.manitoone.domain.QReplyPost;
import com.finalproject.manitoone.domain.QReport;
import com.finalproject.manitoone.domain.QUser;
import com.finalproject.manitoone.domain.ReplyPost;
import com.finalproject.manitoone.domain.Report;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.UserPostLike;
import com.finalproject.manitoone.domain.dto.PostImageResponseDto;
import com.finalproject.manitoone.domain.dto.admin.PostSearchResponseDto;
import com.finalproject.manitoone.domain.dto.admin.ReplyPostSearchResponseDto;
import com.finalproject.manitoone.domain.dto.admin.ReportSearchResponseDto;
import com.finalproject.manitoone.domain.dto.admin.UserProfileRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserProfileResponseDto;
import com.finalproject.manitoone.domain.dto.admin.UserSearchResponseDto;
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
import com.finalproject.manitoone.util.DataUtil;
import com.finalproject.manitoone.util.FileUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminService {

  private static final String PROFILE_IMAGE_DIR = "/usr/local/images/profiles";
  private static final String TEST_DIR = "C:\\test_image\\";

  private final S3Service s3Service;

  private final JPAQueryFactory queryFactory;

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;
  private final AiPostLogRepository aiPostLogRepository;
  private final UserPostLikeRepository userPostLikeRepository;
  private final ReplyPostRepository replyPostRepository;
  private final ManitoLetterRepository manitoLetterRepository;
  private final ManitoMatchesRepository manitoMatchesRepository;
  private final ReportRepository reportRepository;
  private final NotificationRepository notificationRepository;
  private final AiValidationLogRepository aiValidationLogRepository;

  private final FileUtil fileUtil;
  private final DataUtil dataUtil;

  public Page<UserSearchResponseDto> searchUsers(SearchType type, String content, Integer status,
      Pageable pageable) {
    QUser user = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();

    if (!content.isEmpty()) {
      if (type == SearchType.NICKNAME) {
        builder.and(user.nickname.containsIgnoreCase(content));
      }

      if (type == SearchType.EMAIL) {
        builder.and(user.email.containsIgnoreCase(content));
      }

      if (type == SearchType.NAME) {
        builder.and(user.name.containsIgnoreCase(content));
      }
    }

    if (status != null) {
      builder.and(user.status.eq(status));
    }

    // QueryDSL로 페이징 처리
    List<User> users = queryFactory
        .selectFrom(user)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(user.userId.asc())
        .fetch();

    long total = queryFactory
        .selectFrom(user)
        .where(builder)
        .fetchCount();

    List<UserSearchResponseDto> dtoList = users.stream()
        .map(this::toUserSearchResponseDto)
        .toList();

    return new PageImpl<>(dtoList, pageable, total);
  }

  private UserSearchResponseDto toUserSearchResponseDto(User user) {
    return UserSearchResponseDto.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .name(user.getName())
        .nickname(user.getNickname())
        .birth(user.getBirth())
        .introduce(user.getIntroduce())
        .profileImage(user.getProfileImage())
        .status(user.getStatus())
        .role(user.getRole())
        .unbannedAt(user.getUnbannedAt())
        .createdAt(user.getCreatedAt())
        .build();
  }

  public UserProfileResponseDto updateUser(UserProfileRequestDto userProfileRequestDto) {
    User user = userRepository.findById(userProfileRequestDto.getUserId())
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    if (userProfileRequestDto.getEmail() != null) {
      if (isDuplicateEmail(userProfileRequestDto.getEmail())) {
        throw new IllegalArgumentException(IllegalActionMessages.EMAIL_ALREADY_IN_USE.getMessage());
      }
      user = user.toBuilder()
          .email(userProfileRequestDto.getEmail())
          .build();
    }
    if (userProfileRequestDto.getName() != null) {
      user = user.toBuilder()
          .name(userProfileRequestDto.getName())
          .build();
    }
    if (userProfileRequestDto.getNickname() != null) {
      if (isDuplicateNickname(userProfileRequestDto.getNickname())) {
        throw new IllegalArgumentException(
            IllegalActionMessages.NICKNAME_ALREADY_IN_USE.getMessage());
      }
      user = user.toBuilder()
          .nickname(userProfileRequestDto.getNickname())
          .build();
    }
    if (userProfileRequestDto.getBirth() != null) {
      user = user.toBuilder()
          .birth(userProfileRequestDto.getBirth())
          .build();
    }
    if (userProfileRequestDto.getIntroduce() != null) {
      user = user.toBuilder()
          .introduce(userProfileRequestDto.getIntroduce())
          .build();
    }
    if (userProfileRequestDto.getProfileImage() != null) {
      user = user.toBuilder()
          .profileImage(userProfileRequestDto.getProfileImage())
          .build();
    }
    if (userProfileRequestDto.getStatus() != null) {
      user = user.toBuilder()
          .status(userProfileRequestDto.getStatus())
          .build();
    }
    if (userProfileRequestDto.getRole() != null) {
      user = user.toBuilder()
          .role(userProfileRequestDto.getRole())
          .build();
    }
    if (Boolean.TRUE.equals(userProfileRequestDto.getClearUnbannedAt())) {
      user = user.toBuilder()
          .unbannedAt(null)
          .build();
    } else {
      if (userProfileRequestDto.getUnbannedAt() != null) {
        user = user.toBuilder()
            .unbannedAt(userProfileRequestDto.getUnbannedAt())
            .build();
      }
    }

    return toUserProfileResponseDto(userRepository.save(user));
  }

  private UserProfileResponseDto toUserProfileResponseDto(User user) {
    return UserProfileResponseDto.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .name(user.getName())
        .nickname(user.getNickname())
        .birth(user.getBirth())
        .introduce(user.getIntroduce())
        .profileImage(user.getProfileImage())
        .status(user.getStatus())
        .role(user.getRole())
        .unbannedAt(user.getUnbannedAt())
        .createdAt(user.getCreatedAt())
        .build();
  }

  private boolean isDuplicateEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  private boolean isDuplicateNickname(String nickname) {
    return userRepository.existsByNickname(nickname);
  }

  public UserProfileResponseDto updateProfileImage(Long userId, MultipartFile profileImageFile)
      throws IOException {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    if (profileImageFile == null) {
      if (!user.isDefaultImage()) {
        s3Service.deleteImage(user.getProfileImage());
      }
      user.updateDefaultImage();
    } else {
      try {
        s3Service.updateProfileImage(user.getEmail(), profileImageFile);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("이미지 변경을 실패했습니다.");
      }
    }
    return toUserProfileResponseDto(userRepository.save(user));
  }

  public Page<PostSearchResponseDto> searchPosts(SearchType type, String content, Boolean isBlind,
      Pageable pageable) {
    QPost post = QPost.post;

    BooleanBuilder builder = new BooleanBuilder();

    if (!content.isEmpty()) {
      // nickname 조건 (User 기반 검색)
      if (type == SearchType.NICKNAME) {
        builder.and(post.user.nickname.containsIgnoreCase(content));
      }

      // name 조건 (User 기반 검색)
      if (type == SearchType.NAME) {
        builder.and(post.user.name.containsIgnoreCase(content));
      }

      // email 조건 (User 기반 검색)
      if (type == SearchType.EMAIL) {
        builder.and(post.user.email.containsIgnoreCase(content));
      }

      // content 조건 (Post 기반 검색)
      if (type == SearchType.CONTENT) {
        builder.and(post.content.containsIgnoreCase(content));
      }
    }

    // isBlind 조건 (Post 기반 검색)
    if (isBlind != null) {
      builder.and(post.isBlind.eq(isBlind));
    }

    // QueryDSL로 페이징 처리
    List<Post> posts = queryFactory
        .selectFrom(post)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(post.postId.asc())
        .fetch();

    long total = queryFactory
        .selectFrom(post)
        .where(builder)
        .fetchCount();

    List<PostSearchResponseDto> dtoList = posts.stream()
        .map(this::toPostSearchResponseDto)
        .toList();

    return new PageImpl<>(dtoList, pageable, total);
  }

  public List<PostImageResponseDto> getPostImages(Long postId) {
    return postImageRepository.findAllByPost_PostId(postId).orElse(new ArrayList<>()).stream().map(
        postImage -> PostImageResponseDto.builder().postImageId(postImage.getPostImageId())
            .fileName(postImage.getFileName()).build()).toList();
  }

  private PostSearchResponseDto toPostSearchResponseDto(Post post) {
    return PostSearchResponseDto.builder()
        .postId(post.getPostId())
        .user(toUserSearchResponseDto(post.getUser()))
        .content(post.getContent())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .isManito(post.getIsManito())
        .isSelected(post.getIsSelected())
        .isHidden(post.getIsHidden())
        .isBlind(post.getIsBlind())
        .timeDifference(dataUtil.getTimeDifference(post.getCreatedAt()))
        .build();
  }

  public PostSearchResponseDto updateBlind(Long postId) {
    Post post = postRepository.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    post.updateBlind();
    return toPostSearchResponseDto(postRepository.save(post));
  }

  public ReplyPostSearchResponseDto updateBlindReply(Long replyPostId) {
    ReplyPost replyPost = replyPostRepository.findByReplyPostId(replyPostId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_WITH_GIVEN_ID.getMessage()));

    replyPost.updateBlind();
    return toReplySearchResponseDto(replyPostRepository.save(replyPost));
  }

  public void deletePost(Long postId) {
    Post post = postRepository.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    // 게시글 이미지 삭제
    List<PostImage> postImages = postImageRepository.findAllByPostPostId(postId)
        .orElse(new ArrayList<>());
    if (!postImages.isEmpty()) {
      // 이미지 실제 삭제
      for (PostImage postImage : postImages) {
        s3Service.deleteImage(postImage.getFileName());
      }
      postImageRepository.deleteAll(postImages);
    }

    // 게시글 댓글 삭제
    List<ReplyPost> replyPosts = replyPostRepository.findAllByPostPostId(postId)
        .orElse(new ArrayList<>());
    if (!replyPosts.isEmpty()) {
      replyPostRepository.deleteAll(replyPosts);
    }

    // 게시글 AI 피드백 삭제
    aiPostLogRepository.findByPostPostId(postId).ifPresent(aiPostLogRepository::delete);

    // 게시글 AI 벨리데이션 삭제
    List<AiValidationLog> aiValidationLogs = aiValidationLogRepository.findAllByPostPostId(postId).orElse(new ArrayList<>());
    if (!aiValidationLogs.isEmpty()) {
      aiValidationLogRepository.deleteAll(aiValidationLogs);
    }

    // 좋아요 삭제
    List<UserPostLike> userPostLikes = userPostLikeRepository.findAllByPostPostId(postId)
        .orElse(new ArrayList<>());
    if (!userPostLikes.isEmpty()) {
      userPostLikeRepository.deleteAll(userPostLikes);
    }

    // 마니또 연결 삭제
    List<ManitoMatches> manitoMatches = manitoMatchesRepository.findByMatchedPostId(post)
        .orElse(new ArrayList<>());

    if (!manitoMatches.isEmpty()) {
      for (ManitoMatches match : manitoMatches) {
        // 매취스 아이디로 ManitoLetter 찾기
        Optional<ManitoLetter> manitoLetterOptional = manitoLetterRepository.findByManitoMatches_ManitoMatchesId(
            match.getManitoMatchesId());
        if (manitoLetterOptional.isPresent()) {
          // ManitoLetter가 존재하면 삭제
          ManitoLetter manitoLetter = manitoLetterOptional.get();
          manitoLetterRepository.delete(manitoLetter);
        }
        // 매취스 삭제
        manitoMatchesRepository.delete(match);
      }
    }

    // 게시글 신고 목록 삭제
    List<Report> reports = reportRepository.findAllByTypeAndReportObjectId(ReportObjectType.POST,
        postId).orElse(new ArrayList<>());
    if (!reports.isEmpty()) {
      reportRepository.deleteAll(reports);
    }

    // 알림 삭제
    List<Notification> notifications = notificationRepository.findByTypeInAndRelatedObjectId(
            List.of(
                NotiType.LIKE_CLOVER, NotiType.POST_REPLY, NotiType.POST_RE_REPLY), postId)
        .orElse(new ArrayList<>());
    if (!notifications.isEmpty()) {
      notificationRepository.deleteAll(notifications);
    }

    postRepository.delete(post);
  }

  // fixme : 부모 댓글이 없다면 최상위 댓글 자식 댓글 다 삭제 해야하나??
  public void deleteReply(Long replyPostId) {
    ReplyPost replyPost = replyPostRepository.findByReplyPostId(replyPostId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_WITH_GIVEN_ID.getMessage()));

    // 최상위 댓글이라면 자식 댓글 삭제
    List<ReplyPost> replyPosts = replyPostRepository.findAllByParentId(replyPostId)
        .orElse(new ArrayList<>());
    if (!replyPosts.isEmpty()) {
      replyPostRepository.deleteAll(replyPosts);
    }

    // 댓글 신고 목록 삭제
    List<Report> reports = reportRepository.findAllByTypeAndReportObjectId(ReportObjectType.REPLY,
        replyPostId).orElse(new ArrayList<>());
    if (!reports.isEmpty()) {
      reportRepository.deleteAll(reports);
    }

    replyPostRepository.delete(replyPost);
  }

  public Page<ReportSearchResponseDto> searchReports(SearchType type,
      String content,
      ReportObjectType reportObjectType,
      ReportType reportType,
      Pageable pageable) {
    QReport report = QReport.report;
    QPost post = QPost.post;
    QReplyPost replyPost = QReplyPost.replyPost;
    QUser user = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();

    // 신고 타입 필터링 (ReportType)
    if (reportType != null) {
      builder.and(report.reportType.eq(reportType));
    }

    // 신고 대상 타입 필터링 (ReportObjectType)
    if (reportObjectType != null) {
      // 특정 타입이 지정된 경우 해당 타입만 필터링
      builder.and(report.type.eq(reportObjectType));
    } else {
      // 타입이 null인 경우 MANITO_LETTER와 MANITO_ANSWER 모두 포함
      builder.and(
          report.type.in(ReportObjectType.POST, ReportObjectType.REPLY)
      );
    }

    if (!content.isEmpty()) {
      // 신고한 사람 닉네임 검색 (reportedBy)
      if (type == SearchType.REPORTED_BY) {
        builder.and(report.userId.in(
            JPAExpressions.select(user.userId)
                .from(user)
                .where(user.nickname.containsIgnoreCase(content))
        ));
      }

      // 신고당한 사람 닉네임 검색 (reportedTo)
      if (type == SearchType.REPORTED_TO) {
        BooleanBuilder postCondition = new BooleanBuilder();
        BooleanBuilder replyCondition = new BooleanBuilder();

        postCondition.and(report.type.eq(ReportObjectType.POST))
            .and(report.reportObjectId.in(
                JPAExpressions.select(post.postId)
                    .from(post)
                    .where(
                        post.user.nickname.containsIgnoreCase(content))
            ));

        replyCondition.and(report.type.eq(ReportObjectType.REPLY))
            .and(report.reportObjectId.in(
                JPAExpressions.select(replyPost.replyPostId)
                    .from(replyPost)
                    .where(replyPost.user.nickname.containsIgnoreCase(
                        content))
            ));

        builder.and(postCondition.or(replyCondition));
      }

      // 키워드 검색 조건
      if (type == SearchType.CONTENT) {
        if (reportObjectType == ReportObjectType.POST) {
          builder.and(report.type.eq(ReportObjectType.POST))
              .and(report.reportObjectId.in(
                  JPAExpressions.select(post.postId)
                      .from(post)
                      .where(post.content.containsIgnoreCase(content))
              ));
        } else if (reportObjectType == ReportObjectType.REPLY) {
          builder.and(report.type.eq(ReportObjectType.REPLY))
              .and(report.reportObjectId.in(
                  JPAExpressions.select(replyPost.replyPostId)
                      .from(replyPost)
                      .where(
                          replyPost.content.containsIgnoreCase(content))
              ));
        } else {
          BooleanBuilder postCondition = new BooleanBuilder();
          BooleanBuilder replyCondition = new BooleanBuilder();

          postCondition.and(report.type.eq(ReportObjectType.POST))
              .and(report.reportObjectId.in(
                  JPAExpressions.select(post.postId)
                      .from(post)
                      .where(post.content.containsIgnoreCase(content))
              ));

          replyCondition.and(report.type.eq(ReportObjectType.REPLY))
              .and(report.reportObjectId.in(
                  JPAExpressions.select(replyPost.replyPostId)
                      .from(replyPost)
                      .where(
                          replyPost.content.containsIgnoreCase(content))
              ));

          builder.and(postCondition.or(replyCondition));
        }
      }
    }

    List<Report> reports = null;

    reports = queryFactory
        .selectFrom(report)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(report.reportId.asc())
        .fetch();

    long total = queryFactory
        .selectFrom(report)
        .where(builder)
        .fetchCount();

    List<ReportSearchResponseDto> reportDtos = reports.stream()
        .map(this::toReportSearchResponseDto)
        .toList();

    return new PageImpl<>(reportDtos, pageable, total);
  }

  private ReportSearchResponseDto toReportSearchResponseDto(Report report) {
    return ReportSearchResponseDto.builder()
        .reportId(report.getReportId())
        .type(Map.of(
            "data", report.getType().name(),
            "label", report.getType().getType()
        ))
        .content(extractContent(report))
        .reportType(Map.of(
            "data", report.getReportType().name(),
            "label", report.getReportType().getType()
        ))
        .reportedByUser(toUserSearchResponseDto(
            Objects.requireNonNull(
                userRepository.findById(report.getUserId()).orElse(null)))) // 신고한 유저
        .reportObjectId(report.getReportObjectId())
        .createdAt(report.getCreatedAt())
        .post(report.getType() == ReportObjectType.POST
            ? postRepository.findByPostId(report.getReportObjectId())
            .map(this::toPostSearchResponseDto)
            .orElse(null)
            : null)
        .replyPost(report.getType() == ReportObjectType.REPLY
            ? replyPostRepository.findByReplyPostId(report.getReportObjectId())
            .map(this::toReplySearchResponseDto)
            .orElse(null)
            : null)
        .build();
  }

  private ReplyPostSearchResponseDto toReplySearchResponseDto(ReplyPost replyPost) {
    return ReplyPostSearchResponseDto.builder()
        .replyPostId(replyPost.getReplyPostId())
        .post(toPostSearchResponseDto(replyPost.getPost()))
        .user(toUserSearchResponseDto(replyPost.getUser()))
        .parentId(replyPost.getParentId())
        .content(replyPost.getContent())
        .createdAt(replyPost.getCreatedAt())
        .isBlind(replyPost.getIsBlind())
        .timeDifference(dataUtil.getTimeDifference(replyPost.getCreatedAt()))
        .build();
  }

  private String extractContent(Report report) {
    if (report.getType() == ReportObjectType.POST) {
      Post post = postRepository.findByPostId(report.getReportObjectId()).orElse(null);
      return post != null ? post.getContent() : null;
    } else if (report.getType() == ReportObjectType.REPLY) {
      ReplyPost replyPost = replyPostRepository.findByReplyPostId(report.getReportObjectId())
          .orElse(null);
      return replyPost != null ? replyPost.getContent() : null;
    }
    return null;
  }

  public boolean isReportPost(Long postId) {
    return reportRepository.existsByTypeAndReportObjectId(ReportObjectType.POST, postId);
  }

  public boolean isReportReply(Long replyPostId) {
    return reportRepository.existsByTypeAndReportObjectId(ReportObjectType.REPLY, replyPostId);
  }

  public void deleteReport(Long reportId) {
    Report report = reportRepository.findById(reportId).orElseThrow(
        () -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPORT_WITH_GIVEN_ID.getMessage()));

    List<Report> reports = reportRepository.findAllByTypeAndReportObjectId(report.getType(),
        report.getReportObjectId()).orElse(new ArrayList<>());

    if (!reports.isEmpty()) {
      reportRepository.deleteAll(reports);
    }
  }


  public Page<ReportSearchResponseDto> searchManitoReports(SearchType type, String content,
      ReportObjectType reportObjectType, ReportType reportType,
      Pageable pageable) {
    QReport report = QReport.report;
    QPost post = QPost.post;
    QUser user = QUser.user;
    QManitoMatches manitoMatches = QManitoMatches.manitoMatches;
    QManitoLetter manitoLetter = QManitoLetter.manitoLetter;

    BooleanBuilder builder = new BooleanBuilder();

    // 신고 타입 필터링 (ReportType)
    if (reportType != null) {
      builder.and(report.reportType.eq(reportType));
    }

    // 신고 대상 타입 필터링 (ReportObjectType)
    if (reportObjectType != null) {
      // 특정 타입이 지정된 경우 해당 타입만 필터링
      builder.and(report.type.eq(reportObjectType));
    } else {
      // 타입이 null인 경우 MANITO_LETTER와 MANITO_ANSWER 모두 포함
      builder.and(
          report.type.in(ReportObjectType.MANITO_LETTER, ReportObjectType.MANITO_ANSWER)
      );
    }

    if (!content.isEmpty()) {
      // 신고한 사람 닉네임 검색 (reportedBy)
      if (type == SearchType.REPORTED_BY) {
        builder.and(report.userId.in(
            JPAExpressions.select(user.userId)
                .from(user)
                .where(user.nickname.containsIgnoreCase(content))
        ));
      }

      // 신고당한 사람 닉네임 검색 (reportedTo)
      if (type == SearchType.REPORTED_TO) {
        BooleanBuilder reportedToBuilder = new BooleanBuilder();

        if (reportObjectType == ReportObjectType.MANITO_LETTER) {
          // MANITO_LETTER 신고 상황
          reportedToBuilder.or(
              report.type.eq(ReportObjectType.MANITO_LETTER)
                  .and(
                      JPAExpressions.select(manitoMatches.matchedUserId.userId) // 매취스의 유저 (피신고자)
                          .from(manitoLetter)
                          .join(manitoLetter.manitoMatches, manitoMatches)
                          .join(manitoMatches.matchedUserId, user) // User 조인 추가
                          .where(
                              manitoLetter.manitoLetterId.eq(report.reportObjectId) // 마니또 PK
                                  .and(user.nickname.containsIgnoreCase(
                                      content)) // 닉네임 필터링
                          )
                          .exists()
                  )
          );
        } else if (reportObjectType == ReportObjectType.MANITO_ANSWER) {
          // MANITO_ANSWER 신고 상황
          reportedToBuilder.or(
              report.type.eq(ReportObjectType.MANITO_ANSWER)
                  .and(
                      JPAExpressions.select(post.user.userId) // 게시글 작성자 (피신고자)
                          .from(manitoLetter)
                          .join(manitoLetter.manitoMatches, manitoMatches)
                          .join(manitoMatches.matchedPostId, post) // 매취스와 게시글 연결
                          .join(post.user, user) // User 조인 추가
                          .where(
                              manitoLetter.manitoLetterId.eq(report.reportObjectId) // 마니또 PK
                                  .and(user.nickname.containsIgnoreCase(
                                      content)) // 닉네임 필터링
                          )
                          .exists()
                  )
          );
        } else {
          // 전체 검색 (MANITO_LETTER + MANITO_ANSWER)
          reportedToBuilder.or(
              report.type.eq(ReportObjectType.MANITO_LETTER)
                  .and(
                      JPAExpressions.select(manitoMatches.matchedUserId.userId) // 매취스의 유저 (피신고자)
                          .from(manitoLetter)
                          .join(manitoLetter.manitoMatches, manitoMatches)
                          .join(manitoMatches.matchedUserId, user) // User 조인 추가
                          .where(
                              manitoLetter.manitoLetterId.eq(report.reportObjectId) // 마니또 PK
                                  .and(user.nickname.containsIgnoreCase(
                                      content)) // 닉네임 필터링
                          )
                          .exists()
                  )
          );

          reportedToBuilder.or(
              report.type.eq(ReportObjectType.MANITO_ANSWER)
                  .and(
                      JPAExpressions.select(post.user.userId) // 게시글 작성자 (피신고자)
                          .from(manitoLetter)
                          .join(manitoLetter.manitoMatches, manitoMatches)
                          .join(manitoMatches.matchedPostId, post) // 매취스와 게시글 연결
                          .join(post.user, user) // User 조인 추가
                          .where(
                              manitoLetter.manitoLetterId.eq(report.reportObjectId) // 마니또 PK
                                  .and(user.nickname.containsIgnoreCase(
                                      content)) // 닉네임 필터링
                          )
                          .exists()
                  )
          );
        }

        builder.and(reportedToBuilder);
      }

      // 키워드 검색 (content)
      if (type == SearchType.CONTENT) {
        BooleanBuilder contentSearchBuilder = new BooleanBuilder();

        if (reportObjectType == ReportObjectType.MANITO_LETTER) {
          // 마니또 답변 내용으로 검색 (letterContent)
          contentSearchBuilder.or(
              JPAExpressions.selectOne()
                  .from(manitoLetter)
                  .where(
                      manitoLetter.letterContent.containsIgnoreCase(content)
                          .and(manitoLetter.manitoLetterId.eq(report.reportObjectId))
                  )
                  .exists()
          );
        } else if (reportObjectType == ReportObjectType.MANITO_ANSWER) {
          // 마니또 감사 인사 내용으로 검색 (answerLetter)
          contentSearchBuilder.or(
              JPAExpressions.selectOne()
                  .from(manitoLetter)
                  .where(
                      manitoLetter.answerLetter.containsIgnoreCase(content)
                          .and(manitoLetter.manitoLetterId.eq(report.reportObjectId))
                  )
                  .exists()
          );
        } else {
          // 전체 검색
          contentSearchBuilder.and(
              JPAExpressions.selectOne()
                  .from(manitoLetter)
                  .where(
                      manitoLetter.manitoLetterId.eq(report.reportObjectId)
                          .and(
                              report.type.eq(ReportObjectType.MANITO_LETTER)
                                  .and(manitoLetter.letterContent.containsIgnoreCase(content))
                                  .or(
                                      report.type.eq(ReportObjectType.MANITO_ANSWER)
                                          .and(manitoLetter.answerLetter.containsIgnoreCase(content))
                                  )
                          )
                  )
                  .exists()
          );
        }

        builder.and(contentSearchBuilder);
      }
    }

    List<Report> reports;

    reports = queryFactory
        .selectFrom(report)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(report.reportId.asc())
        .fetch();

    long total = queryFactory
        .selectFrom(report)
        .where(builder)
        .fetchCount();

    List<ReportSearchResponseDto> reportDtos = reports.stream()
        .map(this::toManitoReportSearchResponseDto)
        .toList();
    return new PageImpl<>(reportDtos, pageable, total);
  }

  private ReportSearchResponseDto toManitoReportSearchResponseDto(Report report) {
    User ReportedByUser = userRepository.findById(report.getUserId()).orElseThrow(
        () -> new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));
// 신고자 (Reported By)
    UserSearchResponseDto reportedByUser = toUserSearchResponseDto(ReportedByUser);

    // 피신고자 (Reported To) 처리
    UserSearchResponseDto reportedToUser = null;
    ManitoLetter manitoLetter = manitoLetterRepository.findById(report.getReportObjectId())
        .orElseThrow(() -> new IllegalStateException(
            IllegalActionMessages.CANNOT_FIND_MANITO_WITH_GIVEN_ID.getMessage())); // Optional 처리 (null 반환 가능)

    if (manitoLetter != null) {
      if (report.getType() == ReportObjectType.MANITO_LETTER) {
        reportedToUser = toUserSearchResponseDto(
            manitoLetter.getManitoMatches().getMatchedUserId());
      } else {
        reportedToUser = toUserSearchResponseDto(
            manitoLetter.getManitoMatches().getMatchedPostId().getUser());
      }

    }

    // ReportSearchResponseDto 빌더로 변환
    assert manitoLetter != null;
    return ReportSearchResponseDto.builder()
        .reportId(report.getReportId())
        .type(Map.of("data", report.getType().name(), "label", report.getType().getType()))
        .reportType(Map.of("data", report.getReportType().name(), "label",
            report.getReportType().getType()))
        .content(getReportContent(report, manitoLetter)) // 신고 타입에 따라 컨텐츠 결정
        .letter(report.getType() == ReportObjectType.MANITO_ANSWER ? manitoLetter.getLetterContent() : "")
        .reportedByUser(reportedByUser)
        .reportObjectId(report.getReportObjectId())
        .createdAt(report.getCreatedAt())
        .reportedToUser(reportedToUser)
        .post(toPostSearchResponseDto(manitoLetter.getManitoMatches().getMatchedPostId()))
        .build();
  }

  private String getReportContent(Report report, ManitoLetter manitoLetter) {
    if (report.getType() == ReportObjectType.MANITO_LETTER) {
      // 마니또 답변 신고
      return manitoLetter.getLetterContent();
    } else if (report.getType() == ReportObjectType.MANITO_ANSWER) {
      // 감사 인사 신고
      return manitoLetter.getAnswerLetter();
    }
    return null; // 타입이 명확하지 않을 경우 null 반환
  }
}
