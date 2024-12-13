package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.domain.ManitoMatches;
import com.finalproject.manitoone.domain.Post;
import com.finalproject.manitoone.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ManitoMatchesRepository extends JpaRepository<ManitoMatches, Long> {
  // 유저의 24시간 이내 매칭 여부 확인
  @Query("SELECT COUNT(m) > 0 FROM ManitoMatches m " +
      "WHERE m.matchedUserId = :user " +
      "AND m.matchedTime > :timeLimit")
  boolean hasRecentMatch(@Param("user") User user,
      @Param("timeLimit") LocalDateTime timeLimit);

  // 배정 가능한 게시글 목록 조회
  @Query("SELECT DISTINCT p FROM Post p " +
      "WHERE p.isManito = true " +
      "AND p.createdAt > :timeLimit " +
      "AND (NOT EXISTS (SELECT m FROM ManitoMatches m WHERE m.matchedPostId = p) " +
      "OR EXISTS (SELECT m FROM ManitoMatches m " +
      "WHERE m.matchedPostId = p " +
      "AND m.status IN ('REPORTED', 'EXPIRED', 'PASSED'))) " +
      "ORDER BY p.createdAt ASC")
  List<Post> findAssignablePosts(@Param("timeLimit") LocalDateTime timeLimit);

  // 편지 미작성된 MATCHED 상태 매칭 중 24시간 경과된 것 찾기
  @Query("SELECT m FROM ManitoMatches m " +
      "WHERE m.status = 'MATCHED' " +
      "AND m.matchedTime < :timeLimit " +
      "AND NOT EXISTS (SELECT l FROM ManitoLetter l WHERE l.manitoMatches = m)")
  List<ManitoMatches> findExpiredMatchesWithoutLetter(@Param("timeLimit") LocalDateTime timeLimit);

  // 특정 매칭에 대한 편지 존재 여부 확인
  @Query("SELECT COUNT(l) > 0 FROM ManitoLetter l WHERE l.manitoMatches = :match")
  boolean existsLetterForMatch(@Param("match") ManitoMatches match);
}
