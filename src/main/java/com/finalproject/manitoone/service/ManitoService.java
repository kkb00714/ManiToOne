package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.ManitoErrorMessages;
import com.finalproject.manitoone.util.ManitoCommentParser;
import com.finalproject.manitoone.util.TimeFormatter;
import com.finalproject.manitoone.domain.ManitoComment;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.dto.manito.ManitoCommentRequestDto;
import com.finalproject.manitoone.dto.manito.ManitoCommentResponseDto;
import com.finalproject.manitoone.dto.manito.ManitoPageResponseDto;
import com.finalproject.manitoone.repository.ManitoCommentRepository;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ManitoService {

  private final ManitoCommentRepository manitoCommentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  // 마니또 편지 생성
  public ManitoCommentResponseDto createReply(Long postId, ManitoCommentRequestDto requestDto,
      String userNickname) {

    Post post = postRepository.findById(postId)
        .orElseThrow(
            () -> new EntityNotFoundException(ManitoErrorMessages.POST_NOT_FOUND.getMessage()));

    User user = userRepository.findUserByNickname(userNickname)
        .orElseThrow(
            () -> new EntityNotFoundException(ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    validateReplyCreation(post, user);

    ManitoComment manitoComment = requestDto.toEntity(post, user);
    ManitoComment savedComment = manitoCommentRepository.save(manitoComment);

    return buildResponseDto(savedComment, userNickname);
  }

  // 편지 생성 유효성 검사
  private void validateReplyCreation(Post post, User user) {
    if (!post.getIsManito()) {
      throw new IllegalStateException(ManitoErrorMessages.NOT_MANITO_POST.getMessage());
    }

    if (!post.getIsSelected()) {
      throw new IllegalStateException(ManitoErrorMessages.NOT_SELECTED_POST.getMessage());
    }

    if (manitoCommentRepository.findByPostIdAndUser(post, user).isPresent()) {
      throw new IllegalStateException(ManitoErrorMessages.ALREADY_REPLIED.getMessage());
    }

    if (post.getUser().equals(user)) {
      throw new IllegalStateException(ManitoErrorMessages.OWN_POST_REPLY.getMessage());
    }
  }

  private ManitoCommentResponseDto buildResponseDto(ManitoComment comment,
      String currentUserNickname) {
    User currentUser = userRepository.findUserByNickname(currentUserNickname)
        .orElseThrow(
            () -> new EntityNotFoundException(ManitoErrorMessages.USER_NOT_FOUND.getMessage()));

    return ManitoCommentResponseDto.builder()
        .manitoCommentId(comment.getManitoCommentId())
        .content(ManitoCommentParser.extractContent(comment.getComment()))
        .musicUrl(ManitoCommentParser.extractMusicUrl(comment.getComment()))
        .musicComment(ManitoCommentParser.extractMusicComment(comment.getComment()))
        .isPublic(comment.isPublic())
        .isReport(comment.isReport())
        .answerComment(comment.getAnswerComment())
        .timeDiff(TimeFormatter.formatTimeDiff(comment.getCreatedAt()))
        .isOwner(comment.isOwnedBy(currentUser))
        .build();
  }

  public ManitoPageResponseDto getReceiveManito(String nickname, Pageable pageable) {
    Page<ManitoComment> comments = manitoCommentRepository.findByPostId_User_Nickname(nickname,
        pageable);

    return ManitoPageResponseDto.builder()
        .content(comments.getContent().stream()
            .map(comment -> buildResponseDto(comment, nickname))
            .collect(Collectors.toList()))
        .currentPage(comments.getNumber())
        .totalPages(comments.getTotalPages())
        .totalElements(comments.getTotalElements())
        .hasNext(comments.hasNext())
        .build();
  }

  public ManitoPageResponseDto getSendManito(String nickname, Pageable pageable) {
    Page<ManitoComment> comments = manitoCommentRepository.findByUser_Nickname(nickname, pageable);

    return ManitoPageResponseDto.builder()
        .content(comments.getContent().stream()
            .map(comment -> buildResponseDto(comment, nickname))
            .collect(Collectors.toList()))
        .currentPage(comments.getNumber())
        .totalPages(comments.getTotalPages())
        .totalElements(comments.getTotalElements())
        .hasNext(comments.hasNext())
        .build();
  }


  // 편지에 답장
  public ManitoCommentResponseDto answerManitoReply(Long manitoCommentId, String answerComment,
      String userNickname) {
    ManitoComment manitoComment = manitoCommentRepository.findById(manitoCommentId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_REPLY_NOT_FOUND.getMessage()));

    manitoComment.addAnswer(answerComment, userNickname);

    return buildResponseDto(manitoComment, userNickname);
  }

  // 편지 신고
  public void reportManitoReply(Long manitoCommentId, String userNickname) {
    ManitoComment manitoComment = manitoCommentRepository.findById(manitoCommentId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_REPLY_NOT_FOUND.getMessage()));

    if (!manitoComment.getPostId().getUser().getNickname().equals(userNickname)) {
      throw new IllegalStateException(ManitoErrorMessages.NO_PERMISSION_REPORT.getMessage());
    }

    manitoComment.reportComment();
  }

  // 편지 공개 토글
  public void toggleManitoReplyVisibility(Long manitoCommentId, String userNickname) {
    ManitoComment manitoComment = manitoCommentRepository.findById(manitoCommentId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_REPLY_NOT_FOUND.getMessage()));

    if (!manitoComment.getPostId().getUser().getNickname().equals(userNickname)) {
      throw new IllegalStateException(ManitoErrorMessages.NO_PERMISSION_VISIBILITY.getMessage());
    }

    manitoComment.toggleVisibility(userNickname);
  }

  // 답장 신고
  public void reportManitoAnswer(Long manitoCommentId, String userNickname) {
    ManitoComment manitoComment = manitoCommentRepository.findById(manitoCommentId)
        .orElseThrow(() -> new EntityNotFoundException(
            ManitoErrorMessages.MANITO_REPLY_NOT_FOUND.getMessage()));

    manitoComment.reportAnswer(userNickname);
  }
}
