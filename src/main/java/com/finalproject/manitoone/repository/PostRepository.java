package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.Post;
import java.time.LocalDateTime;
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

  Optional<Post> findByPostIdAndIsHiddenFalseAndIsBlindFalse(Long postId);

  Optional<List<Post>> findAllByUserUserId(Long userId);

  Optional<Page<Post>> findAllByIsHiddenFalseAndIsBlindFalse(Pageable pageable);

  // 타임라인 조회를 위한 쿼리
  @Query("SELECT p FROM Post p WHERE (" +
      "p.user.userId IN (SELECT f.following.userId FROM Follow f WHERE f.follower.userId = :userId) " +
      "OR p.user.userId = :userId) " +
      "AND p.isBlind = false AND p.isHidden = false " +
      "ORDER BY p.createdAt DESC")
  Page<Post> findTimelinePostsByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query(value = """
    WITH RankedRecentPosts AS (
        SELECT p.*, 
               ROW_NUMBER() OVER (ORDER BY RAND()) as rn
        FROM post p
        WHERE p.user_id NOT IN (
            SELECT f.following_id 
            FROM follow f 
            WHERE f.follower_id = :userId
        )
        AND p.post_id NOT IN :excludePostIds
        AND p.user_id != :userId
        AND p.is_blind = false 
        AND p.is_hidden = false
        AND p.created_at > :recentPeriod
    )
    SELECT * FROM RankedRecentPosts 
    WHERE rn <= :limit
    ORDER BY created_at DESC
    """, nativeQuery = true)
  List<Post> findRandomRecentPosts(
      @Param("userId") Long userId,
      @Param("recentPeriod") LocalDateTime recentPeriod,
      @Param("limit") int limit,
      @Param("excludePostIds") List<Long> excludePostIds
  );
}
