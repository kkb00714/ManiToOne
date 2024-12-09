package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.ReplyPost;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AddReplyRequestDto;
import com.finalproject.manitoone.domain.dto.ReplyResponseDto;
import com.finalproject.manitoone.repository.PostRepository;
import com.finalproject.manitoone.repository.ReplyPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplyService {

  private final PostRepository postRepository;
  private final ReplyPostRepository replyPostRepository;

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

    return new ReplyResponseDto(reply.getPost(), reply.getUser(), reply.getParentId(),
        reply.getContent(), reply.getCreatedAt(), reply.getIsBlind());
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
        .parentId(parentReply.getParentId())
        .build());

    return new ReplyResponseDto(childReply.getPost(), childReply.getUser(),
        childReply.getParentId(), childReply.getContent(), childReply.getCreatedAt(),
        childReply.getIsBlind());
  }
}
