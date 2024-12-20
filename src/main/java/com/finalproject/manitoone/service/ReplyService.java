package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.constants.NotiType;
import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.constants.ReportType;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.ReplyPost;
import com.finalproject.manitoone.domain.Report;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.UserPostLike;
import com.finalproject.manitoone.domain.dto.ReplyResponseDto;
import com.finalproject.manitoone.domain.dto.ReportResponseDto;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.ReplyPostRepository;
import com.finalproject.manitoone.repository.ReportRepository;
import com.finalproject.manitoone.repository.UserPostLikeRepository;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.util.NotificationUtil;
import com.finalproject.manitoone.util.TimeFormatter;
import java.io.IOException;
import java.sql.Time;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyService {

  private final PostRepository postRepository;
  private final ReplyPostRepository replyPostRepository;
  private final ReportRepository reportRepository;
  private final UserPostLikeRepository userPostLikeRepository;
  private final UserRepository userRepository;

  private final NotificationUtil notificationUtil;

  // 답글 생성
  public ReplyResponseDto createReply(Long postId, String content, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    Post post = postRepository.findByPostIdAndIsHiddenFalseAndIsBlindFalse(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    ReplyPost reply = replyPostRepository.save(ReplyPost.builder()
        .post(post)
        .user(user)
        .content(content)
        .build());

    // 내 게시글에 내가 답글 남길때는 알림 생성 x
    if (!Objects.equals(user.getUserId(), post.getUser().getUserId())) {
      try {
        notificationUtil.createNotification(post.getUser().getNickname(), user, NotiType.POST_REPLY,
            post.getPostId());
      } catch (IOException e) {
        log.error(e.getMessage());
      }
    }

    return ReplyResponseDto.builder()
        .post(reply.getPost())
        .user(reply.getUser())
        .replyPostId(reply.getReplyPostId())
        .content(reply.getContent())
        .createdAt(reply.getCreatedAt())
        .isBlind(reply.getIsBlind())
        .build();
  }

  // 답글의 답글 생성
  public ReplyResponseDto createReReply(Long replyId, String content, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    ReplyPost parentReply = replyPostRepository.findByReplyPostIdAndIsBlindFalseAndIsHiddenFalse(
            replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    ReplyPost childReply = replyPostRepository.save(ReplyPost.builder()
        .post(parentReply.getPost())
        .user(user)
        .content(content)
        .parentId(parentReply.getReplyPostId())
        .build());

    // 부모 답글이 자신인데 자신이 달 경우는 알림이 안간다.
    if (!Objects.equals(user.getUserId(), parentReply.getUser().getUserId())) {
      try {
        notificationUtil.createNotification(parentReply.getUser().getNickname(), user,
            NotiType.POST_RE_REPLY,
            parentReply.getPost().getPostId());
      } catch (IOException e) {
        log.error(e.getMessage());
      }
    }

    return ReplyResponseDto.builder()
        .post(childReply.getPost())
        .user(childReply.getUser())
        .parentId(childReply.getParentId())
        .replyPostId(childReply.getReplyPostId())
        .content(childReply.getContent())
        .createdAt(childReply.getCreatedAt())
        .isBlind(childReply.getIsBlind())
        .build();
  }

  // 답글 수정
  public ReplyResponseDto updateReply(Long replyId, String content, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    ReplyPost reply = replyPostRepository.findByReplyPostIdAndIsBlindFalseAndIsHiddenFalse(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    if (!reply.getUser().equals(user)) {
      throw new IllegalArgumentException(IllegalActionMessages.DIFFERENT_USER.getMessage());
    }

    reply.updateReply(content);

    ReplyPost updatedReply = replyPostRepository.save(reply);

    return ReplyResponseDto.builder()
        .post(updatedReply.getPost())
        .user(updatedReply.getUser())
        .parentId(updatedReply.getParentId())
        .replyPostId(updatedReply.getReplyPostId())
        .content(updatedReply.getContent())
        .createdAt(updatedReply.getCreatedAt())
        .build();
  }

  // 답글 삭제
  public void deleteReply(Long replyId, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    ReplyPost reply = replyPostRepository.findByReplyPostIdAndIsBlindFalseAndIsHiddenFalse(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    if (!reply.getUser().equals(user)) {
      throw new IllegalArgumentException(
          IllegalActionMessages.CANNOT_DELETE_POST_AND_REPLY.getMessage());
    }

    replyPostRepository.delete(reply);
  }

  // 답글 신고
  public ReportResponseDto reportReply(Long replyId, String reportType, String email) {
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

    ReplyPost reply = replyPostRepository.findByReplyPostIdAndIsBlindFalseAndIsHiddenFalse(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    Report report = reportRepository.save(Report.builder()
        .reportType(theType)
        .userId(user.getUserId())
        .type(ReportObjectType.REPLY)
        .reportObjectId(reply.getReplyPostId())
        .build());

    return ReportResponseDto.builder()
        .reportId(report.getReportId())
        .userId(report.getUserId())
        .reportObjectId(report.getReportObjectId())
        .reportType(report.getReportType())
        .type(report.getType())
        .build();
  }

  // 답글 좋아요
  public ReplyResponseDto likeReply(Long replyId, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    ReplyPost reply = replyPostRepository.findByReplyPostIdAndIsBlindFalseAndIsHiddenFalse(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    Optional<UserPostLike> existingLike = userPostLikeRepository.findByUserUserIdAndPostPostIdAndReplyPostId(
        user.getUserId(), reply.getPost().getPostId(), reply.getReplyPostId());

    if (existingLike.isPresent()) {
      userPostLikeRepository.delete(existingLike.get());
    } else {
      userPostLikeRepository.save(UserPostLike.builder()
          .post(reply.getPost())
          .user(user)
          .replyPostId(reply.getReplyPostId())
          .build());
    }

    return ReplyResponseDto.builder()
        .replyPostId(reply.getReplyPostId())
        .content(reply.getContent())
        .likesNumber(getReplyLikesNum(reply.getReplyPostId()))
        .build();
  }

  // 답글 숨기기
  public void hideReply(Long replyId, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
        IllegalActionMessages.CANNOT_FIND_USER_WITH_GIVEN_ID.getMessage()
    ));

    ReplyPost reply = replyPostRepository.findByReplyPostIdAndIsBlindFalseAndIsHiddenFalse(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    if (!reply.getUser().equals(user)) {
      throw new IllegalArgumentException(IllegalActionMessages.CANNOT_HIDE_POST.getMessage());
    }

    reply.hideReply();

    replyPostRepository.save(reply);
  }

  // 게시글 답글 조회
  public Page<ReplyResponseDto> getReplies(Long postId, Pageable pageable) {
    Page<ReplyPost> replies = replyPostRepository.findAllByPostPostIdAndParentIdIsNullAndIsBlindFalseAndIsHiddenFalse(
            postId, pageable)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    return replies.map(reply -> new ReplyResponseDto(
        reply.getPost(),
        reply.getUser(),
        reply.getParentId(),
        reply.getReplyPostId(),
        reply.getContent(),
        reply.getCreatedAt(),
        TimeFormatter.formatTimeDiff(reply.getCreatedAt()),
        reply.getIsBlind(),
        getReRepliesNum(reply.getReplyPostId()),
        getReplyLikesNum(reply.getReplyPostId())
    ));
  }

  // 답글 개수 조회
  public Integer getRepliesNum(Long postId) {
    List<ReplyPost> replies = replyPostRepository.findAllByPostPostIdAndParentIdNullAndIsBlindFalseAndIsHiddenFalse(
            postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    return replies.size();
  }

  // 답글 단건 조회
  public ReplyResponseDto getReply(Long replyId) {
    ReplyPost reply = replyPostRepository.findByReplyPostIdAndIsBlindFalseAndIsHiddenFalse(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    return ReplyResponseDto.builder()
        .post(reply.getPost())
        .user(reply.getUser())
        .parentId(reply.getParentId())
        .replyPostId(reply.getReplyPostId())
        .content(reply.getContent())
        .createdAt(reply.getCreatedAt())
        .createdDiff(TimeFormatter.formatTimeDiff(reply.getCreatedAt()))
        .isBlind(reply.getIsBlind())
        .rerepliesNumber(getReRepliesNum(reply.getReplyPostId()))
        .likesNumber(getReplyLikesNum(reply.getReplyPostId()))
        .build();
  }

  // 답글의 답글 조회
  public Page<ReplyResponseDto> getReReplies(Long replyId, Pageable pageable) {
    Page<ReplyPost> rereplies = replyPostRepository.findAllByParentIdAndIsBlindFalseAndIsHiddenFalse(
            replyId,
            pageable)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    return rereplies.map(rereply -> new ReplyResponseDto(
        rereply.getPost(),
        rereply.getUser(),
        rereply.getParentId(),
        rereply.getReplyPostId(),
        rereply.getContent(),
        rereply.getCreatedAt(),
        TimeFormatter.formatTimeDiff(rereply.getCreatedAt()),
        rereply.getIsBlind(),
        getReRepliesNum(rereply.getReplyPostId()),
        getReplyLikesNum(rereply.getReplyPostId())
    ));
  }

  // 답글의 답글 개수 조회
  public Integer getReRepliesNum(Long replyId) {
    List<ReplyPost> rereplies = replyPostRepository.findAllByParentIdAndIsBlindFalseAndIsHiddenFalse(
            replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    return rereplies.size();
  }

  // 답글 좋아요 개수 조회
  public Integer getReplyLikesNum(Long replyId) {
    List<UserPostLike> likes = userPostLikeRepository.findAllByReplyPostId(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_USER_POST_LIKE_WITH_GIVEN_ID.getMessage()
        ));

    return likes.size();
  }
}
