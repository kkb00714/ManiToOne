package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.UserPostLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPostLikeRepository extends JpaRepository<UserPostLike, Long> {

  Optional<List<UserPostLike>> findAllByUser_nicknameAndPost_IsHiddenFalseAndPost_IsBlindFalse(
      String nickName, Pageable pageable);

  Optional<Integer> countAllByPost_PostId(Long postId);

  Optional<List<UserPostLike>> findAllByPostPostId(Long postId);

  Optional<List<UserPostLike>> findAllByPostPostIdAndReplyPostIdNull(Long postId);

  Optional<List<UserPostLike>> findAllByReplyPostId(Long replyId);

  Optional<UserPostLike> findByUser_UserIdAndPost_PostId(Long userId, Long postId);

  Optional<UserPostLike> findByUserUserIdAndPostPostIdAndReplyPostId(Long userId, Long postId,
      Long replyPostId);
}
