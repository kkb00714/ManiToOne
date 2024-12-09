package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.ReplyPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyPostRepository extends JpaRepository<ReplyPost, Long> {

  Optional<List<ReplyPost>> findAllByPost_PostIdAndIsBlindFalse(Long postId);

  Optional<List<ReplyPost>> findAllByPostPostId(Long postId);

  Optional<ReplyPost> findByReplyPostId(Long replyPostId);

  Optional<List<ReplyPost>> findAllByPostPostIdAndParentIdIsNull(Long postId);

  Optional<List<ReplyPost>> findAllByPostPostIdAndParentIdIsNotNull(Long postId);
}
