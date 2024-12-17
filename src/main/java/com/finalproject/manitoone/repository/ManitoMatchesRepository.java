package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.constants.MatchStatus;
import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ManitoMatchesRepository extends JpaRepository<ManitoMatches, Long> {

  Optional<List<ManitoMatches>> findAllByMatchedPostId_PostId(Long postId);

  // 유저의 24시간 이내 매칭 여부 확인
  @Query("SELECT COUNT(m) > 0 FROM ManitoMatches m " +
      "WHERE m.matchedUserId = :user " +
      "AND m.matchedTime > :timeLimit")
  boolean hasRecentMatch(@Param("user") User user,
      @Param("timeLimit") LocalDateTime timeLimit);

  // 배정 가능한 포스트 조회
  @Query("SELECT DISTINCT p FROM Post p " +
      "WHERE p.isManito = true " +
      "AND p.createdAt > :timeLimit " +
      "AND p.user.userId != :userId " +
      "AND (NOT EXISTS (SELECT 1 FROM ManitoMatches m WHERE m.matchedPostId = p) " +
      "OR EXISTS (SELECT 1 FROM ManitoMatches m " +
      "          WHERE m.matchedPostId = p " +
      "          AND m.matchedTime = (SELECT MAX(m2.matchedTime) FROM ManitoMatches m2 WHERE m2.matchedPostId = p) " +
      "          AND m.status IN ('REPORTED', 'EXPIRED', 'PASSED'))) " +
      "ORDER BY p.createdAt ASC")
  List<Post> findAssignablePosts(
      @Param("timeLimit") LocalDateTime timeLimit,
      @Param("userId") Long userId);

  // 유저의 가장 최근 매칭 조회 (24시간 이내)
  @Query("SELECT m FROM ManitoMatches m " +
      "LEFT JOIN FETCH m.matchedPostId p " +
      "LEFT JOIN FETCH m.matchedUserId u " +
      "LEFT JOIN FETCH p.user " +
      "WHERE m.matchedUserId.userId = :userId " +
      "AND m.matchedTime > :timeLimit " +
      "ORDER BY m.matchedTime DESC")
  Optional<ManitoMatches> findLatestMatchByUser(
      @Param("userId") Long userId,
      @Param("timeLimit") LocalDateTime timeLimit);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT COUNT(m) > 0 FROM ManitoMatches m WHERE m.matchedPostId = :post AND m.status = :status")
  boolean existsByMatchedPostIdAndStatus(@Param("post") Post post, @Param("status") MatchStatus status);

  // 24시간 이상 경과된 MATCHED 상태의 ManitoMatches 중 ManitoLetter가 없는 것 찾기
  @Query("""
        SELECT m
        FROM ManitoMatches m
        WHERE m.status = 'MATCHED'
          AND m.matchedTime <= :deadline
          AND NOT EXISTS (
              SELECT l
              FROM ManitoLetter l
              WHERE l.manitoMatches = m
          )
    """)
  List<ManitoMatches> findUnansweredMatches(LocalDateTime deadline);

  Optional<List<ManitoMatches>> findByMatchedPostId(Post post);
}
