package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.ReplyPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyPostRepository extends JpaRepository<ReplyPost, Long> {

  Optional<List<ReplyPost>> findAllByPost_PostIdAndIsBlindFalse(Long postId);

  Optional<List<ReplyPost>> findAllByPostPostId(Long postId);

  Optional<ReplyPost> findByReplyPostId(Long replyId);

  Optional<ReplyPost> findByReplyPostIdAndIsBlindFalseAndIsHiddenFalse(Long replyPostId);

  Optional<Page<ReplyPost>> findAllByPostPostIdAndParentIdIsNullAndIsBlindFalseAndIsHiddenFalse(
      Long postId,
      Pageable pageable);

  Optional<List<ReplyPost>> findAllByPostPostIdAndParentIdNullAndIsBlindFalseAndIsHiddenFalse(
      Long postId);

  Optional<Page<ReplyPost>> findAllByReplyPostIdAndParentIdIsNotNull(Long replyId,
      Pageable pageable);

  Optional<List<ReplyPost>> findAllByReplyPostIdAndParentIdIsNotNull(Long replyId);

  Optional<Page<ReplyPost>> findAllByParentIdAndIsBlindFalseAndIsHiddenFalse(Long parentId,
      Pageable pageable);

  Optional<List<ReplyPost>> findAllByPostPostIdAndParentIdIsNotNull(Long postId);

  Optional<List<ReplyPost>> findAllByParentId(Long parentId);

  Optional<List<ReplyPost>> findAllByParentIdAndIsBlindFalseAndIsHiddenFalse(Long parentId);
}
