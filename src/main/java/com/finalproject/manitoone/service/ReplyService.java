package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.ReplyPost;
import com.finalproject.manitoone.domain.Report;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.UserPostLike;
import com.finalproject.manitoone.domain.dto.AddReplyRequestDto;
import com.finalproject.manitoone.domain.dto.AddReportRequestDto;
import com.finalproject.manitoone.domain.dto.ReplyResponseDto;
import com.finalproject.manitoone.domain.dto.ReportResponseDto;
import com.finalproject.manitoone.domain.dto.UpdateReplyRequestDto;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.ReplyPostRepository;
import com.finalproject.manitoone.repository.ReportRepository;
import com.finalproject.manitoone.repository.UserPostLikeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyService {

  private final PostRepository postRepository;
  private final ReplyPostRepository replyPostRepository;
  private final ReportRepository reportRepository;
  private final UserPostLikeRepository userPostLikeRepository;

  // 답글 생성
  public ReplyResponseDto createReply(Long postId, AddReplyRequestDto request, User user) {
    Post post = postRepository.findByPostId(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_POST_WITH_GIVEN_ID.getMessage()));

    ReplyPost reply = replyPostRepository.save(ReplyPost.builder()
        .post(post)
        .user(user)
        .content(request.getContent())
        .parentId(post.getPostId())
        .build());

    return ReplyResponseDto.builder()
        .post(reply.getPost())
        .user(reply.getUser())
        .parentId(reply.getParentId())
        .content(reply.getContent())
        .createdAt(reply.getCreatedAt())
        .isBlind(reply.getIsBlind())
        .build();
  }

  // 답글의 답글 생성
  public ReplyResponseDto createReReply(Long replyId, AddReplyRequestDto request, User user) {
    ReplyPost parentReply = replyPostRepository.findByReplyPostId(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    ReplyPost childReply = replyPostRepository.save(ReplyPost.builder()
        .post(parentReply.getPost())
        .user(user)
        .content(request.getContent())
        .parentId(parentReply.getReplyPostId())
        .build());

    return ReplyResponseDto.builder()
        .post(childReply.getPost())
        .user(childReply.getUser())
        .parentId(childReply.getParentId())
        .content(childReply.getContent())
        .createdAt(childReply.getCreatedAt())
        .isBlind(childReply.getIsBlind())
        .build();
  }

  // 답글 수정
  public ReplyResponseDto updateReply(Long replyId, UpdateReplyRequestDto request, User user) {
    ReplyPost reply = replyPostRepository.findByReplyPostId(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    if (!reply.getUser().equals(user)) {
      throw new IllegalArgumentException(IllegalActionMessages.DIFFERENT_USER.getMessage());
    }

    reply.updateReply(request.getContent());

    ReplyPost updatedReply = replyPostRepository.save(reply);

    return ReplyResponseDto.builder()
        .post(updatedReply.getPost())
        .user(updatedReply.getUser())
        .parentId(updatedReply.getParentId())
        .content(updatedReply.getContent())
        .createdAt(updatedReply.getCreatedAt())
        .isBlind(updatedReply.getIsBlind())
        .build();
  }

  // 답글 삭제
  public void deleteReply(Long replyId, User user) {
    ReplyPost reply = replyPostRepository.findByReplyPostId(replyId)
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
  public ReportResponseDto reportReply(Long replyId, AddReportRequestDto request, User user) {
    ReplyPost reply = replyPostRepository.findByReplyPostId(replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    Report report = reportRepository.save(Report.builder()
        .reportType(request.getReportType())
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
//  public void likeReply(Long replyId, User user) {
//    ReplyPost reply = replyPostRepository.findByReplyPostId(replyId)
//        .orElseThrow(() -> new IllegalArgumentException(
//            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
//        ));
//
//    userPostLikeRepository.save(UserPostLike.builder()
//        .user(user)
//        .replyPost(reply)
//        .build());
//  }

  // 답글 조회
  public Page<ReplyResponseDto> getReplies(Long postId, Pageable pageable) {
    Page<ReplyPost> replies = replyPostRepository.findAllByPostPostIdAndParentIdIsNull(postId,
            pageable)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    return replies.map(reply -> new ReplyResponseDto(
        reply.getPost(),
        reply.getUser(),
        reply.getParentId(),
        reply.getContent(),
        reply.getCreatedAt(),
        reply.getIsBlind(),
        getReRepliesNum(reply.getReplyPostId())
//        getReplyLikesNum(reply.getReplyPostId())
    ));
  }

  // 답글 개수 조회
  public Integer getRepliesNum(Long postId) {
    List<ReplyPost> replies = replyPostRepository.findAllByPostPostIdAndParentIdIsNull(postId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    return replies.size();
  }

  // 답글의 답글 조회
  public Page<ReplyResponseDto> getReReplies(Long replyId, Pageable pageable) {
    Page<ReplyPost> rereplies = replyPostRepository.findAllByReplyPostIdAndParentIdIsNotNull(
            replyId, pageable)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    return rereplies.map(rereply -> new ReplyResponseDto(
        rereply.getPost(),
        rereply.getUser(),
        rereply.getParentId(),
        rereply.getContent(),
        rereply.getCreatedAt(),
        rereply.getIsBlind(),
        getReRepliesNum(rereply.getReplyPostId())
//        getReplyLikesNum(rereply.getReplyPostId())
    ));
  }

  // 답글의 답글 개수 조회
  public Integer getReRepliesNum(Long replyId) {
    List<ReplyPost> rereplies = replyPostRepository.findAllByReplyPostIdAndParentIdIsNotNull(
            replyId)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_REPLY_POST_WITH_GIVEN_ID.getMessage()
        ));

    return rereplies.size();
  }

  // 답글 좋아요 개수 조회
//  public Integer getReplyLikesNum(Long replyId) {
//    List<UserPostLike> likes = userPostLikeRepository.findAllByReplyPostReplyPostId(replyId)
//        .orElseThrow(() -> new IllegalArgumentException(
//            IllegalActionMessages.CANNOT_FIND_USER_POST_LIKE_WITH_GIVEN_ID.getMessage()
//        ));
//
//    return likes.size();
//  }
}
