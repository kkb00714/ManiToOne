package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  Optional<List<Post>> findAllByIsBlindFalseAndIsHiddenFalseAndUser_Nickname(String nickName,
      Pageable pageable);

  Optional<List<Post>> findAllByIsBlindFalseAndIsHiddenTrueAndUser_Nickname(String nickName,
      Pageable pageable);

  Optional<Post> findByPostId(Long postId);

  Optional<List<Post>> findAllByUserUserId(Long userId);

  Optional<Page<Post>> findAllByIsHiddenFalseAndIsBlindFalse(Pageable pageable);

  // 타임라인 조회를 위한 쿼리
  @Query("SELECT DISTINCT p FROM Post p " +
      "JOIN Follow f ON f.following.userId = p.user.userId " +
      "WHERE f.follower.userId = :userId " +
      "AND p.isBlind = false AND p.isHidden = false " +
      "AND NOT EXISTS (SELECT r FROM ReplyPost r WHERE r.post = p AND r.parentId IS NOT NULL) " +
      "ORDER BY p.createdAt DESC")
  Page<Post> findTimelinePostsByUserId(@Param("userId") Long userId, Pageable pageable);
}
