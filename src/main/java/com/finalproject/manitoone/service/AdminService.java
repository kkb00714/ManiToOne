package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.domain.ManitoLetter;
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
import com.finalproject.manitoone.domain.dto.admin.PostSearchRequestDto;
import com.finalproject.manitoone.domain.dto.admin.PostSearchResponseDto;
import com.finalproject.manitoone.domain.dto.admin.ReplyPostSearchResponseDto;
import com.finalproject.manitoone.domain.dto.admin.ReportSearchRequestDto;
import com.finalproject.manitoone.domain.dto.admin.ReportSearchResponseDto;
import com.finalproject.manitoone.domain.dto.admin.UserProfileRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserProfileResponseDto;
import com.finalproject.manitoone.domain.dto.admin.UserSearchRequestDto;
import com.finalproject.manitoone.domain.dto.admin.UserSearchResponseDto;
import com.finalproject.manitoone.repository.AiPostLogRepository;
import com.finalproject.manitoone.repository.ManitoLetterRepository;
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
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminService {

  private static final String PROFILE_IMAGE_DIR = "/home/profile/images/";
  private static final String TEST_DIR = "C:\\test_image\\";

  private final JPAQueryFactory queryFactory;

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;
  private final AiPostLogRepository aiPostLogRepository;
  private final UserPostLikeRepository userPostLikeRepository;
  private final ReplyPostRepository replyPostRepository;
  private final ManitoLetterRepository manitoLetterRepository;
  private final ReportRepository reportRepository;

  private final FileUtil fileUtil;
  private final DataUtil dataUtil;

  public Page<UserSearchResponseDto> searchUsers(UserSearchRequestDto userSearchRequestDto,
      Pageable pageable) {
    QUser user = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();

    if (userSearchRequestDto.getNickname() != null && !userSearchRequestDto.getNickname()
        .isEmpty()) {
      builder.and(user.nickname.containsIgnoreCase(userSearchRequestDto.getNickname()));
    }

    if (userSearchRequestDto.getEmail() != null && !userSearchRequestDto.getEmail().isEmpty()) {
      builder.and(user.email.containsIgnoreCase(userSearchRequestDto.getEmail()));
    }

    if (userSearchRequestDto.getName() != null && !userSearchRequestDto.getName().isEmpty()) {
      builder.and(user.name.containsIgnoreCase(userSearchRequestDto.getName()));
    }

    if (userSearchRequestDto.getStatus() != null) {
      builder.and(user.status.eq(userSearchRequestDto.getStatus()));
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

  public UserProfileResponseDto updateProfileImage(Long userId, MultipartFile profileImageFile) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()));

    if (user.getProfileImage() != null && !user.isDefaultImage()) {
      String existingImagePath = user.getProfileImage(); // 기존 이미지의 절대 경로를 구성
      File existingImageFile = new File(existingImagePath);

      if (existingImageFile.exists()) {
        fileUtil.cleanUp(Paths.get(existingImagePath));
      }
    }

    if (profileImageFile == null) {
      user.updateDefaultImage();
    } else {
      String uploadDir = TEST_DIR;
      File uploadDirectory = new File(uploadDir);

      if (!uploadDirectory.exists()) {
        fileUtil.createDir(Paths.get(uploadDir));
      }

      String uniqueFileName = UUID.randomUUID() + "_" + profileImageFile.getOriginalFilename();
      String finalFilePath = uploadDir + uniqueFileName;

      fileUtil.save(Paths.get(finalFilePath), profileImageFile);
      user.updateProfileImage(finalFilePath);
    }
    return toUserProfileResponseDto(userRepository.save(user));
  }

  public Page<PostSearchResponseDto> searchPosts(PostSearchRequestDto postSearchRequestDto,
      Pageable pageable) {
    QPost post = QPost.post;

    BooleanBuilder builder = new BooleanBuilder();

    // nickname 조건 (User 기반 검색)
    if (postSearchRequestDto.getNickname() != null && !postSearchRequestDto.getNickname()
        .isEmpty()) {
      builder.and(post.user.nickname.containsIgnoreCase(postSearchRequestDto.getNickname()));
    }

    // name 조건 (User 기반 검색)
    if (postSearchRequestDto.getName() != null && !postSearchRequestDto.getName().isEmpty()) {
      builder.and(post.user.name.containsIgnoreCase(postSearchRequestDto.getName()));
    }

    // email 조건 (User 기반 검색)
    if (postSearchRequestDto.getEmail() != null && !postSearchRequestDto.getEmail().isEmpty()) {
      builder.and(post.user.email.containsIgnoreCase(postSearchRequestDto.getEmail()));
    }

    // content 조건 (Post 기반 검색)
    if (postSearchRequestDto.getContent() != null && !postSearchRequestDto.getContent().isEmpty()) {
      builder.and(post.content.containsIgnoreCase(postSearchRequestDto.getContent()));
    }

    // isBlind 조건 (Post 기반 검색)
    if (postSearchRequestDto.getIsBlind() != null) {
      builder.and(post.isBlind.eq(postSearchRequestDto.getIsBlind()));
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
        fileUtil.cleanUp(Paths.get(postImage.getFileName()));
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

    // 좋아요 삭제
    List<UserPostLike> userPostLikes = userPostLikeRepository.findAllByPostPostId(postId)
        .orElse(new ArrayList<>());
    if (!userPostLikes.isEmpty()) {
      userPostLikeRepository.deleteAll(userPostLikes);
    }

    // 마니또 연결 삭제
    // fixme : 컬럼이 많이 바뀌어서 추후에 주석 해제
//    manitoLetterRepository.findByPostId(post).ifPresent(manitoLetterRepository::delete);

    // 게시글 신고 목록 삭제
    List<Report> reports = reportRepository.findAllByTypeAndReportObjectId(ReportObjectType.POST,
        postId).orElse(new ArrayList<>());
    if (!reports.isEmpty()) {
      reportRepository.deleteAll(reports);
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

  public Page<ReportSearchResponseDto> searchReports(ReportSearchRequestDto reportSearchRequestDto,
      Pageable pageable) {
    QReport report = QReport.report;
    QPost post = QPost.post;
    QReplyPost replyPost = QReplyPost.replyPost;
    QUser user = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();

    // 신고 타입 필터링 (ReportType)
    if (reportSearchRequestDto.getReportType() != null) {
      builder.and(report.reportType.eq(reportSearchRequestDto.getReportType()));
    }

    // 신고 대상 타입 필터링 (ReportObjectType)
    if (reportSearchRequestDto.getType() != null) {
      // 특정 타입이 지정된 경우 해당 타입만 필터링
      builder.and(report.type.eq(reportSearchRequestDto.getType()));
    } else {
      // 타입이 null인 경우 MANITO_LETTER와 MANITO_ANSWER 모두 포함
      builder.and(
          report.type.in(ReportObjectType.POST, ReportObjectType.REPLY)
      );
    }

    // 신고한 사람 닉네임 검색 (reportedBy)
    if (reportSearchRequestDto.getReportedBy() != null && !reportSearchRequestDto.getReportedBy()
        .isEmpty()) {
      builder.and(report.userId.in(
          JPAExpressions.select(user.userId)
              .from(user)
              .where(user.nickname.containsIgnoreCase(reportSearchRequestDto.getReportedBy()))
      ));
    }

    // 신고당한 사람 닉네임 검색 (reportedTo)
    if (reportSearchRequestDto.getReportedTo() != null && !reportSearchRequestDto.getReportedTo()
        .isEmpty()) {
      BooleanBuilder postCondition = new BooleanBuilder();
      BooleanBuilder replyCondition = new BooleanBuilder();

      postCondition.and(report.type.eq(ReportObjectType.POST))
          .and(report.reportObjectId.in(
              JPAExpressions.select(post.postId)
                  .from(post)
                  .where(
                      post.user.nickname.containsIgnoreCase(reportSearchRequestDto.getReportedTo()))
          ));

      replyCondition.and(report.type.eq(ReportObjectType.REPLY))
          .and(report.reportObjectId.in(
              JPAExpressions.select(replyPost.replyPostId)
                  .from(replyPost)
                  .where(replyPost.user.nickname.containsIgnoreCase(
                      reportSearchRequestDto.getReportedTo()))
          ));

      builder.and(postCondition.or(replyCondition));
    }

    // 키워드 검색 조건
    if (reportSearchRequestDto.getContent() != null && !reportSearchRequestDto.getContent()
        .isEmpty()) {
      if (reportSearchRequestDto.getType() == ReportObjectType.POST) {
        builder.and(report.type.eq(ReportObjectType.POST))
            .and(report.reportObjectId.in(
                JPAExpressions.select(post.postId)
                    .from(post)
                    .where(post.content.containsIgnoreCase(reportSearchRequestDto.getContent()))
            ));
      } else if (reportSearchRequestDto.getType() == ReportObjectType.REPLY) {
        builder.and(report.type.eq(ReportObjectType.REPLY))
            .and(report.reportObjectId.in(
                JPAExpressions.select(replyPost.replyPostId)
                    .from(replyPost)
                    .where(
                        replyPost.content.containsIgnoreCase(reportSearchRequestDto.getContent()))
            ));
      } else {
        BooleanBuilder postCondition = new BooleanBuilder();
        BooleanBuilder replyCondition = new BooleanBuilder();

        postCondition.and(report.type.eq(ReportObjectType.POST))
            .and(report.reportObjectId.in(
                JPAExpressions.select(post.postId)
                    .from(post)
                    .where(post.content.containsIgnoreCase(reportSearchRequestDto.getContent()))
            ));

        replyCondition.and(report.type.eq(ReportObjectType.REPLY))
            .and(report.reportObjectId.in(
                JPAExpressions.select(replyPost.replyPostId)
                    .from(replyPost)
                    .where(
                        replyPost.content.containsIgnoreCase(reportSearchRequestDto.getContent()))
            ));

        builder.and(postCondition.or(replyCondition));
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


  public Page<ReportSearchResponseDto> searchManitoReports(
      ReportSearchRequestDto reportSearchRequestDto,
      Pageable pageable) {
    QReport report = QReport.report;
    QPost post = QPost.post;
    QUser user = QUser.user;
    QManitoMatches manitoMatches = QManitoMatches.manitoMatches;
    QManitoLetter manitoLetter = QManitoLetter.manitoLetter;

    BooleanBuilder builder = new BooleanBuilder();

    // 신고 타입 필터링 (ReportType)
    if (reportSearchRequestDto.getReportType() != null) {
      builder.and(report.reportType.eq(reportSearchRequestDto.getReportType()));
    }

    // 신고 대상 타입 필터링 (ReportObjectType)
    if (reportSearchRequestDto.getType() != null) {
      // 특정 타입이 지정된 경우 해당 타입만 필터링
      builder.and(report.type.eq(reportSearchRequestDto.getType()));
    } else {
      // 타입이 null인 경우 MANITO_LETTER와 MANITO_ANSWER 모두 포함
      builder.and(
          report.type.in(ReportObjectType.MANITO_LETTER, ReportObjectType.MANITO_ANSWER)
      );
    }

    // 신고한 사람 닉네임 검색 (reportedBy)
    if (reportSearchRequestDto.getReportedBy() != null && !reportSearchRequestDto.getReportedBy()
        .isEmpty()) {
      builder.and(report.userId.in(
          JPAExpressions.select(user.userId)
              .from(user)
              .where(user.nickname.containsIgnoreCase(reportSearchRequestDto.getReportedBy()))
      ));
    }

    // 신고당한 사람 닉네임 검색 (reportedTo)
    if (reportSearchRequestDto.getReportedTo() != null && !reportSearchRequestDto.getReportedTo()
        .isEmpty()) {
      BooleanBuilder reportedToBuilder = new BooleanBuilder();

      if (reportSearchRequestDto.getType() == ReportObjectType.MANITO_LETTER) {
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
                                    reportSearchRequestDto.getReportedTo())) // 닉네임 필터링
                        )
                        .exists()
                )
        );
      } else if (reportSearchRequestDto.getType() == ReportObjectType.MANITO_ANSWER) {
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
                                    reportSearchRequestDto.getReportedTo())) // 닉네임 필터링
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
                                    reportSearchRequestDto.getReportedTo())) // 닉네임 필터링
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
                                    reportSearchRequestDto.getReportedTo())) // 닉네임 필터링
                        )
                        .exists()
                )
        );
      }

      builder.and(reportedToBuilder);
    }

    // 키워드 검색 (content)
    if (reportSearchRequestDto.getContent() != null && !reportSearchRequestDto.getContent()
        .isEmpty()) {
      BooleanBuilder contentSearchBuilder = new BooleanBuilder();

      String keyword = reportSearchRequestDto.getContent().toLowerCase();

      if (reportSearchRequestDto.getType() == ReportObjectType.MANITO_LETTER) {
        // 마니또 답변 내용으로 검색 (letterContent)
        contentSearchBuilder.or(
            JPAExpressions.selectOne()
                .from(manitoLetter)
                .where(
                    manitoLetter.letterContent.containsIgnoreCase(keyword)
                        .and(manitoLetter.manitoLetterId.eq(report.reportObjectId))
                )
                .exists()
        );
      } else if (reportSearchRequestDto.getType() == ReportObjectType.MANITO_ANSWER) {
        // 마니또 감사 인사 내용으로 검색 (answerLetter)
        contentSearchBuilder.or(
            JPAExpressions.selectOne()
                .from(manitoLetter)
                .where(
                    manitoLetter.answerLetter.containsIgnoreCase(keyword)
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
                                .and(manitoLetter.letterContent.containsIgnoreCase(keyword))
                                .or(
                                    report.type.eq(ReportObjectType.MANITO_ANSWER)
                                        .and(manitoLetter.answerLetter.containsIgnoreCase(keyword))
                                )
                        )
                )
                .exists()
        );
      }

      builder.and(contentSearchBuilder);
    }

    JPQLQuery<Report> query = queryFactory
        .selectFrom(report)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(report.reportId.asc());

    System.out.println("Generated Query: " + query); // 디버깅용 쿼리 출력

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
